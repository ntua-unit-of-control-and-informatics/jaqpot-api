package org.jaqpot.api.service.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.*
import org.jaqpot.api.mapper.*
import org.jaqpot.api.model.*
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.repository.util.FullTextUtil
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.dataset.csv.CSVDataConverter
import org.jaqpot.api.service.dataset.csv.CSVParser
import org.jaqpot.api.service.model.config.ModelConfiguration
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
const val JAQPOT_METADATA_KEY = "jaqpotMetadata"
const val JAQPOT_ROW_ID_KEY = "jaqpotRowId"
const val JAQPOT_ROW_LABEL_KEY = "jaqpotRowLabel"

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
    private val storageService: StorageService,
    private val doaService: DoaService,
    private val modelConfiguration: ModelConfiguration
) : ModelApiDelegate {

    companion object {
        val FORBIDDEN_MODEL_TYPES_FOR_CREATION = setOf(
            ModelTypeDto.QSAR_TOOLBOX_CALCULATOR,
            ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL,
            ModelTypeDto.QSAR_TOOLBOX_PROFILER,
            ModelTypeDto.TORCHSCRIPT, // https://github.com/ntua-unit-of-control-and-informatics/jaqpotpy-inference/security/code-scanning/1
            ModelTypeDto.R_BNLEARN_DISCRETE,
            ModelTypeDto.R_CARET,
            ModelTypeDto.R_GBM,
            ModelTypeDto.R_NAIVE_BAYES,
            ModelTypeDto.R_PBPK,
            ModelTypeDto.R_RF,
            ModelTypeDto.R_RPART,
            ModelTypeDto.R_SVM,
            ModelTypeDto.R_TREE_CLASS,
            ModelTypeDto.R_TREE_REGR
        )
    }

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

        if (FORBIDDEN_MODEL_TYPES_FOR_CREATION.contains(modelDto.type)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "${modelDto.type} is not supported for creation.")
        }

        val creatorId = authenticationFacade.userId
        val toEntity = modelDto.toEntity(creatorId)
        val savedModel = modelRepository.save(toEntity)
        storeRawModelToStorage(savedModel)
        storeRawPreprocessorToStorage(savedModel)
        savedModel.doas.forEach(doaService::storeRawDoaToStorage)

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(savedModel.id).toUri()
        return ResponseEntity.created(location).build()
    }

    private fun storeRawModelToStorage(model: Model) {
        if (model.rawModel == null) {
            // model is already stored in storage
            return
        }
        logger.info { "Storing raw model to storage for model with id ${model.id}" }
        if (storageService.storeRawModel(model)) {
            logger.info { "Successfully moved raw model to storage for model ${model.id}" }
            modelRepository.setRawModelToNull(model.id)
        }
    }

    private fun storeRawPreprocessorToStorage(model: Model) {
        if (model.rawPreprocessor == null) {
            // model is already stored in storage
            return
        }
        logger.info { "Storing raw preprocessor to storage for model with id ${model.id}" }
        if (storageService.storeRawPreprocessor(model)) {
            logger.info { "Successfully moved raw model to storage for model ${model.id}" }
            modelRepository.setRawPreprocessorToNull(model.id)
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
    @WithRateLimitProtectionByUser(
        limit = 30,
        intervalInSeconds = 60 * 60
    ) // 30 requests per hour, up to 100 predictions per request
    override fun predictWithModel(modelId: Long, datasetDto: DatasetDto): ResponseEntity<Unit> {
        if (datasetDto.type == DatasetTypeDto.PREDICTION) {
            val model = modelRepository.findById(modelId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            }
            // TODO once there are no models with rawModel in the database, remove this
            storeRawModelToStorage(model)
            // TODO once there are no models with rawPreprocessor in the database, remove this
            storeRawPreprocessorToStorage(model)

            val userId = authenticationFacade.userId
            val toEntity = datasetDto.toEntity(
                model,
                userId,
                DatasetEntryType.ARRAY
            )

            if (toEntity.input!!.size > modelConfiguration.maxInputPredictionRows.toInt()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Input contains more than ${modelConfiguration.maxInputPredictionRows} rows, please provide a smaller dataset"
                )
            }

            toEntity.input.forEachIndexed { index, it: Any ->
                if (it is Map<*, *>)
                    (it as MutableMap<String, String>)[JAQPOT_ROW_ID_KEY] = index.toString()
            }

            val dataset = this.datasetRepository.save(toEntity)

            return triggerPredictionAndReturnSuccessStatus(model, dataset)
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown dataset type", null)
    }

    @PreAuthorize("@predictModelAuthorizationLogic.decide(#root, #modelId)")
    @WithRateLimitProtectionByUser(
        limit = 30,
        intervalInSeconds = 60 * 60
    ) // 30 requests per hour, up to 100 predictions per request
    override fun predictWithModelCSV(modelId: Long, datasetCSVDto: DatasetCSVDto): ResponseEntity<Unit> {
        if (datasetCSVDto.type == DatasetTypeDto.PREDICTION) {
            val model = modelRepository.findById(modelId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            }
            val userId = authenticationFacade.userId
            // TODO once there are no models with rawModel in the database, remove this
            storeRawModelToStorage(model)
            // TODO once there are no models with rawPreprocessor in the database, remove this
            storeRawPreprocessorToStorage(model)

            val csvData = csvParser.readCsv(datasetCSVDto.inputFile.inputStream())

            if (csvData.size > modelConfiguration.maxInputPredictionRows.toInt()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV file contains more than ${modelConfiguration.maxInputPredictionRows} rows, please provide a smaller dataset"
                )
            }

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

    private fun triggerPredictionAndReturnSuccessStatus(
        model: Model,
        dataset: Dataset
    ): ResponseEntity<Unit> {
        val rawModel = if (model.isQsarToolboxModel()) {
            byteArrayOf()
        } else {
            storageService.readRawModel(model)
        }
        val rawPreprocessor = storageService.readRawPreprocessor(model)

        val doaDtos = model.doas.map {
            val rawDoaData = storageService.readRawDoa(it)
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val doaData: Map<String, Any> = Gson().fromJson(rawDoaData.decodeToString(), type)
            it.toPredictionDto(doaData)
        }

        this.predictionService.executePredictionAndSaveResults(
            model.toPredictionModelDto(rawModel, doaDtos, rawPreprocessor),
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

        logger.info { "Updating model with id $id by user ${user.id} and name ${user.username}" }

        val neededOrganizationsIds: List<Long> = partiallyUpdateModelRequestDto.sharedWithOrganizationIds ?: listOf()

        val neededOrganizationsDictionary =
            organizationRepository.findAllById(neededOrganizationsIds)
                .associateBy { it.id!! }

        partiallyUpdateModelRequestDto.name.let { existingModel.name = it }
        partiallyUpdateModelRequestDto.visibility.let { existingModel.visibility = it.toEntity() }
        partiallyUpdateModelRequestDto.tags.let { existingModel.tags = it }
        partiallyUpdateModelRequestDto.description?.let { existingModel.description = it }
        partiallyUpdateModelRequestDto.task.let { existingModel.task = it.toEntity() }


        if (partiallyUpdateModelRequestDto.visibility == ModelVisibilityDto.ORG_SHARED) {
            partiallyUpdateModelRequestDto.sharedWithOrganizationIds?.let {
                existingModel.sharedWithOrganizations.clear()
                existingModel.sharedWithOrganizations.addAll(it.map { organizationId ->
                    val organization = neededOrganizationsDictionary[organizationId]!!
                    ModelOrganizationAssociation(
                        null,
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

