package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.*
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.mapper.toGetModels200ResponseDto
import org.jaqpot.api.mapper.toPredictionModelDto
import org.jaqpot.api.model.*
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.repository.util.FullTextUtil
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.dataset.csv.CSVDataConverter
import org.jaqpot.api.service.dataset.csv.CSVParser
import org.jaqpot.api.service.prediction.PredictionService
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.jaqpot.api.service.util.SortUtil.Companion.parseSortParameters
import org.jaqpot.api.storage.StorageService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class ModelService(
    private val authenticationFacade: AuthenticationFacade,
    private val modelRepository: ModelRepository,
    private val userService: UserService,
    private val predictionService: PredictionService,
    private val datasetRepository: DatasetRepository,
    private val organizationRepository: OrganizationRepository,
    private val csvParser: CSVParser,
    private val csvDataConverter: CSVDataConverter,
    private val storageService: StorageService
) : ModelApiDelegate {


    override fun getModels(page: Int, size: Int, sort: List<String>?): ResponseEntity<GetModels200ResponseDto> {
        val creatorId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))
        val modelsPage = modelRepository.findAllByCreatorId(creatorId, pageable)
        val creator = userService.getUserById(creatorId).orElse(UserDto(creatorId))

        return ResponseEntity.ok().body(modelsPage.toGetModels200ResponseDto(creator))
    }

    override fun getSharedModels(
        page: Int,
        size: Int,
        sort: List<String>?,
        organizationId: Long?
    ): ResponseEntity<GetModels200ResponseDto> {
        val userId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))

        val sharedModelsPage = if (organizationId == null) {
            modelRepository.findAllSharedWithUser(userId, pageable)
        } else {
            modelRepository.findAllSharedWithUserByOrganizationId(userId, pageable, organizationId)
        }

        return ResponseEntity.ok().body(sharedModelsPage.toGetModels200ResponseDto(null))
    }

    @CacheEvict("searchModels", allEntries = true)
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60)
    override fun createModel(modelDto: ModelDto): ResponseEntity<Unit> {
        if (modelDto.id != null) {
            throw IllegalStateException("ID should not be provided for resource creation.")
        }
        val creatorId = authenticationFacade.userId
        val toEntity = modelDto.toEntity(creatorId)
        val savedModel = modelRepository.save(toEntity)
        storeActualModelToStorage(savedModel)
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(savedModel.id).toUri()
        return ResponseEntity.created(location).build()
    }

    private fun storeActualModelToStorage(model: Model) {
        if (model.actualModel == null) {
            // model is already stored in storage
            return
        }
        logger.info { "Storing actual model to storage for model with id ${model.id}" }
        if (storageService.storeRawModel(model)) {
            logger.info { "Successfully moved actual model to storage for model ${model.id}" }
            modelRepository.setActualModelToNull(model.id)
        }
    }

    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    override fun getModelById(id: Long): ResponseEntity<ModelDto> {
        val model = modelRepository.findById(id)

        return model.map {
            val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, it)
            val isAdmin = authenticationFacade.isAdmin
            val user = userService.getUserById(it.creatorId).orElse(UserDto(it.creatorId))
            ResponseEntity.ok(it.toDto(user, userCanEdit, isAdmin))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    override fun getLegacyModelById(id: String): ResponseEntity<ModelDto> {
        val model = modelRepository.findOneByLegacyId(id)

        return model.map {
            val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, it)
            val user = userService.getUserById(it.creatorId).orElse(UserDto(it.creatorId))
            ResponseEntity.ok(it.toDto(user, userCanEdit))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    private fun isCreator(authenticationFacade: AuthenticationFacade, model: Model): Boolean {
        return authenticationFacade.userId == model.creatorId
    }

    @PreAuthorize("@predictModelAuthorizationLogic.decide(#root, #modelId)")
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60)
    override fun predictWithModelCSV(modelId: Long, datasetCSVDto: DatasetCSVDto): ResponseEntity<Unit> {
        if (datasetCSVDto.type == DatasetTypeDto.PREDICTION) {
            val model = modelRepository.findById(modelId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            }
            val userId = authenticationFacade.userId
            storeActualModelToStorage(model)

            val csvData = csvParser.readCsv(datasetCSVDto.inputFile.inputStream())
            val input = csvDataConverter.convertCsvContentToInput(model, csvData)
            val dataset = this.datasetRepository.save(
                datasetCSVDto.toEntity(
                    model,
                    userId,
                    DatasetEntryType.ARRAY,
                    input
                )
            )

            return triggerPredictionAndReturnSuccessStatus(model, dataset)
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown dataset type", null)
    }

    @PreAuthorize("@predictModelAuthorizationLogic.decide(#root, #modelId)")
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60)
    override fun predictWithModel(modelId: Long, datasetDto: DatasetDto): ResponseEntity<Unit> {
        if (datasetDto.type == DatasetTypeDto.PREDICTION) {
            val model = modelRepository.findById(modelId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            }
            storeActualModelToStorage(model)

            val userId = authenticationFacade.userId
            val dataset = this.datasetRepository.save(
                datasetDto.toEntity(
                    model,
                    userId,
                    DatasetEntryType.ARRAY
                )
            )

            return triggerPredictionAndReturnSuccessStatus(model, dataset)
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown dataset type", null)
    }

    private fun triggerPredictionAndReturnSuccessStatus(
        model: Model,
        dataset: Dataset
    ): ResponseEntity<Unit> {
        val rawModel = storageService.readRawModel(model)

        this.predictionService.executePredictionAndSaveResults(
            model.toPredictionModelDto(rawModel),
            dataset
        )

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/datasets/{id}")
            .buildAndExpand(dataset.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @WithRateLimitProtectionByUser(limit = 10, intervalInSeconds = 60)
    @PreAuthorize("@partialModelUpdateAuthorizationLogic.decide(#root, #id, #partiallyUpdateModelRequestDto)")
    @Transactional
    override fun partiallyUpdateModel(
        id: Long,
        partiallyUpdateModelRequestDto: PartiallyUpdateModelRequestDto
    ): ResponseEntity<ModelDto> {
        val existingModel = modelRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $id not found")
        }

        val user = userService.getUserById(authenticationFacade.userId).orElse(UserDto(authenticationFacade.userId))

        logger.info { "Updating model with id $id by user ${user.id} and name ${user.name}" }

        val neededOrganizationsIds: List<Long> =
            (partiallyUpdateModelRequestDto.affiliatedOrganizationIds
                ?: listOf()) + (partiallyUpdateModelRequestDto.sharedWithOrganizationIds ?: listOf())

        val neededOrganizationsDictionary =
            organizationRepository.findAllById(neededOrganizationsIds)
                .associateBy { it.id!! }

        partiallyUpdateModelRequestDto.name.let { existingModel.name = it }
        partiallyUpdateModelRequestDto.visibility.let { existingModel.visibility = it.toEntity() }
        partiallyUpdateModelRequestDto.tags.let { existingModel.tags = it }
        partiallyUpdateModelRequestDto.description?.let { existingModel.description = it }
        if (authenticationFacade.isAdmin) {
            // only allow admins to update affiliated organizations
            partiallyUpdateModelRequestDto.affiliatedOrganizationIds?.let {
                // throws if user has no access or 404 if organization does not exist

                existingModel.affiliatedOrganizations.clear()
                existingModel.affiliatedOrganizations.addAll(
                    it.map { organizationId ->
                        val organization = neededOrganizationsDictionary[organizationId]!!
                        ModelOrganizationAssociation(
                            existingModel,
                            organization,
                            ModelOrganizationAssociationType.AFFILIATION
                        )
                    }
                )
            }
        }

        if (partiallyUpdateModelRequestDto.visibility == ModelVisibilityDto.ORG_SHARED) {
            partiallyUpdateModelRequestDto.sharedWithOrganizationIds?.let {
                existingModel.sharedWithOrganizations.clear()
                existingModel.sharedWithOrganizations.addAll(it.map { organizationId ->
                    val organization = neededOrganizationsDictionary[organizationId]!!
                    ModelOrganizationAssociation(
                        existingModel,
                        organization,
                        ModelOrganizationAssociationType.SHARE
                    )
                })
            }
        } else {
            existingModel.sharedWithOrganizations.clear()
        }

        val model: Model = modelRepository.save(existingModel)

        val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, model)
        val isAdmin = authenticationFacade.isAdmin
        val modelCreator = userService.getUserById(model.creatorId).orElse(UserDto(model.creatorId))
        return ResponseEntity.ok(model.toDto(modelCreator, userCanEdit, isAdmin))
    }

    @PreAuthorize("@getAllAffiliatedModelsAuthorizationLogic.decide(#root, #orgName)")
    override fun getAllAffiliatedModels(
        orgName: String,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ResponseEntity<GetModels200ResponseDto> {
        val organization = organizationRepository.findByName(orgName).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with name $orgName not found")
        }

        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))
        val modelsPage = modelRepository.findAllByAffiliatedOrganizations(organization.id!!, pageable)
        return ResponseEntity.ok(modelsPage.toGetModels200ResponseDto(null))
    }

    @Cacheable(CacheKeys.SEARCH_MODELS)
    override fun searchModels(
        query: String,
        page: Int,
        size: Int
    ): ResponseEntity<GetModels200ResponseDto> {
        val transformedQuery = FullTextUtil.transformSearchQuery(query)
        val pageable = PageRequest.of(page, size)
        val modelsPage = modelRepository.searchModelsBy(transformedQuery, pageable)
        return ResponseEntity.ok(modelsPage.toGetModels200ResponseDto(null))
    }

    @CacheEvict("searchModels", allEntries = true)
    @PreAuthorize("hasAuthority('admin')")
    override fun deleteModelById(id: Long): ResponseEntity<Unit> {
        modelRepository.delete(modelRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $id not found")
        })
        return ResponseEntity.noContent().build()
    }
}

