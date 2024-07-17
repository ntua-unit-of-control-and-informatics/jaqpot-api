package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetEntryType
import org.jaqpot.api.entity.Model
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
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.jaqpot.api.storage.StorageService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

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


    override fun getModels(page: Int, size: Int): ResponseEntity<GetModels200ResponseDto> {
        val creatorId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size)
        val modelsPage = modelRepository.findAllByCreatorId(creatorId, pageable)
        val creator = userService.getUserById(creatorId).orElse(UserDto(creatorId))

        return ResponseEntity.ok().body(modelsPage.toGetModels200ResponseDto(creator))
    }

    override fun getSharedModels(page: Int, size: Int): ResponseEntity<GetModels200ResponseDto> {
        val creatorId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size)
        val sharedModelsPage = modelRepository.findAllSharedWithUser(creatorId, pageable)

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
        if (storageService.storeRawModel(toEntity)) {
            toEntity.actualModel = null
        }
        val model = modelRepository.save(toEntity)
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(model.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    override fun getModelById(id: Long): ResponseEntity<ModelDto> {
        val model = modelRepository.findById(id)

        return model.map {
            val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, it)
            val userCanDelete = authenticationFacade.isAdmin
            val user = userService.getUserById(it.creatorId).orElse(UserDto(it.creatorId))
            ResponseEntity.ok(it.toDto(user, userCanEdit, userCanDelete))
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

            val csvData = csvParser.readCsv(datasetCSVDto.inputFile.inputStream())
            val input = csvDataConverter.convertCsvContentToDataEntry(model, csvData)
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
            model.toPredictionModelDto(Base64.getEncoder().encode(rawModel)),
            dataset
        )

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/datasets/{id}")
            .buildAndExpand(dataset.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @WithRateLimitProtectionByUser(limit = 10, intervalInSeconds = 60)
    @PreAuthorize("@partialModelUpdateAuthorizationLogic.decide(#root, #id)")
    @Transactional
    override fun partiallyUpdateModel(
        id: Long,
        partiallyUpdateModelRequestDto: PartiallyUpdateModelRequestDto
    ): ResponseEntity<ModelDto> {
        val existingModel = modelRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $id not found")
        }
        partiallyUpdateModelRequestDto.name.let { existingModel.name = it }
        partiallyUpdateModelRequestDto.visibility.let { existingModel.visibility = it.toEntity() }
        partiallyUpdateModelRequestDto.description?.let { existingModel.description = it }
        if (partiallyUpdateModelRequestDto.visibility == ModelVisibilityDto.ORG_SHARED) {
            partiallyUpdateModelRequestDto.organizationIds?.let {
                val organizations = organizationRepository.findAllById(it)
                existingModel.organizations.clear()
                existingModel.organizations.addAll(organizations)
            }
        } else {
            existingModel.organizations.clear()
        }

        val model: Model = modelRepository.save(existingModel)

        val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, model)
        val userCanDelete = authenticationFacade.isAdmin
        val user = userService.getUserById(model.creatorId).orElse(UserDto(model.creatorId))
        return ResponseEntity.ok(model.toDto(user, userCanEdit, userCanDelete))
    }

    @Cacheable(CacheKeys.SEARCH_MODELS)
    override fun searchModels(query: String, page: Int, size: Int): ResponseEntity<GetModels200ResponseDto> {
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

