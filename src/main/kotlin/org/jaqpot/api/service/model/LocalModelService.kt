package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.GetModelDownloadUrl200ResponseDto
import org.jaqpot.api.model.GetModelPreprocessorDownloadUrl200ResponseDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authorization.GetModelAuthorizationLogic
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.jaqpot.api.storage.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Service for local model development operations.
 * 
 * This service provides functionality for downloading models and preprocessors
 * for local development and testing using presigned URLs.
 * 
 * Separated from ModelService to keep concerns focused:
 * - ModelService: Core model CRUD operations
 * - LocalModelService: Local development support (downloads, testing)
 */
@Service
class LocalModelService {

    @Autowired
    private lateinit var modelRepository: ModelRepository

    @Autowired
    private lateinit var storageService: StorageService

    @Autowired
    private lateinit var getModelAuthorizationLogic: GetModelAuthorizationLogic

    @Autowired
    private lateinit var environment: Environment

    @Value("\${server.port:8080}")
    private lateinit var serverPort: String

    @Value("\${server.servlet.context-path:}")
    private lateinit var contextPath: String

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Get a presigned URL to download the ONNX model file for local inference.
     * 
     * This endpoint enables local model development by providing secure, time-limited
     * download URLs for model files stored in S3. Supports both small models 
     * (stored in database) and large models (stored in S3).
     * 
     * @param modelId The ID of the model to get download URL for
     * @param expirationMinutes URL expiration time in minutes (default 10, max 60)
     * @return Presigned download URL with expiration timestamp
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 30, intervalInSeconds = 60)
    fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
        logger.info { "Generating download URL for model $modelId (profile: ${environment.activeProfiles.contentToString()})" }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val isLocalProfile = environment.activeProfiles.contains("local")
        val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())

        return if (isLocalProfile && model.rawModel != null) {
            // Local profile: Return URL that points to our direct download endpoint
            val localDownloadUrl = "http://localhost.jaqpot.org:$serverPort$contextPath/v1/models/$modelId/download/local"
            logger.info { "Generated local download URL for model $modelId: $localDownloadUrl" }
            
            ResponseEntity.ok(
                GetModelDownloadUrl200ResponseDto(
                    downloadUrl = URI(localDownloadUrl),
                    expiresAt = expiresAt
                )
            )
        } else {
            // Production profile or no model in database: Use S3 presigned URL
            try {
                val modelContentLength = storageService.readRawModelContentLength(model)
                val downloadUrl = storageService.getPreSignedModelDownloadUrl(model, effectiveExpirationMinutes)
                
                logger.info { "Successfully generated S3 presigned URL for model $modelId, expires at $expiresAt" }
                
                ResponseEntity.ok(
                    GetModelDownloadUrl200ResponseDto(
                        downloadUrl = URI(downloadUrl),
                        expiresAt = expiresAt
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate S3 presigned download URL for model $modelId" }
                
                if (isLocalProfile && model.rawModel != null) {
                    // Fallback to local endpoint even in error case for local profile
                    val localDownloadUrl = "http://localhost.jaqpot.org:$serverPort$contextPath/v1/models/$modelId/download/local"
                    logger.info { "Fallback to local download URL for model $modelId: $localDownloadUrl" }
                    
                    ResponseEntity.ok(
                        GetModelDownloadUrl200ResponseDto(
                            downloadUrl = URI(localDownloadUrl),
                            expiresAt = expiresAt
                        )
                    )
                } else {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model file not found in storage")
                }
            }
        }
    }

    /**
     * Get a presigned URL to download the preprocessor file for local inference.
     * 
     * This endpoint provides access to preprocessing pipelines stored with models,
     * enabling local development workflows that require the same preprocessing
     * as production inference.
     * 
     * @param modelId The ID of the model to get preprocessor download URL for
     * @param expirationMinutes URL expiration time in minutes (default 10, max 60)
     * @return Presigned download URL with expiration timestamp
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 30, intervalInSeconds = 60)
    fun getModelPreprocessorDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto> {
        logger.info { "Generating preprocessor download URL for model $modelId (profile: ${environment.activeProfiles.contentToString()})" }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val isLocalProfile = environment.activeProfiles.contains("local")
        val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())

        return if (isLocalProfile && model.rawPreprocessor != null) {
            // Local profile: Return URL that points to our direct download endpoint
            val localDownloadUrl = "http://localhost.jaqpot.org:$serverPort$contextPath/v1/models/$modelId/preprocessor/download/local"
            logger.info { "Generated local preprocessor download URL for model $modelId: $localDownloadUrl" }
            
            ResponseEntity.ok(
                GetModelPreprocessorDownloadUrl200ResponseDto(
                    downloadUrl = URI(localDownloadUrl),
                    expiresAt = expiresAt
                )
            )
        } else {
            // Production profile or no preprocessor in database: Use S3 presigned URL
            try {
                val preprocessor = storageService.readRawPreprocessor(model)
                if (preprocessor == null) {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor not found for model $modelId")
                }

                val downloadUrl = storageService.getPreSignedPreprocessorDownloadUrl(model, effectiveExpirationMinutes)
                
                logger.info { "Successfully generated S3 presigned preprocessor URL for model $modelId, expires at $expiresAt" }
                
                ResponseEntity.ok(
                    GetModelPreprocessorDownloadUrl200ResponseDto(
                        downloadUrl = URI(downloadUrl),
                        expiresAt = expiresAt
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate S3 presigned preprocessor download URL for model $modelId" }
                
                if (isLocalProfile && model.rawPreprocessor != null) {
                    // Fallback to local endpoint for local profile
                    val localDownloadUrl = "http://localhost.jaqpot.org:$serverPort$contextPath/v1/models/$modelId/preprocessor/download/local"
                    logger.info { "Fallback to local preprocessor download URL for model $modelId: $localDownloadUrl" }
                    
                    ResponseEntity.ok(
                        GetModelPreprocessorDownloadUrl200ResponseDto(
                            downloadUrl = URI(localDownloadUrl),
                            expiresAt = expiresAt
                        )
                    )
                } else {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor not found for model $modelId")
                }
            }
        }
    }

    /**
     * Get model metadata for local development.
     * 
     * Provides essential model information needed for local inference setup,
     * including model type, features, and preprocessing requirements.
     * 
     * @param modelId The ID of the model to get metadata for
     * @return Model metadata suitable for local development
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 60, intervalInSeconds = 60)
    fun getModelMetadataForLocal(modelId: Long): ResponseEntity<Model> {
        logger.info { "Retrieving model metadata for local development: model $modelId" }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        logger.debug { "Retrieved metadata for model $modelId: type=${model.type}, features=${model.independentFeatures.size}" }
        
        return ResponseEntity.ok(model)
    }

    /**
     * Check if model supports local inference.
     * 
     * Validates that a model has all necessary components for local development:
     * - ONNX model file available
     * - Supported model type
     * - Compatible feature definitions
     * 
     * @param modelId The ID of the model to check
     * @return Compatibility information for local inference
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 60, intervalInSeconds = 60)
    fun checkLocalInferenceCompatibility(modelId: Long): ResponseEntity<Map<String, Any>> {
        logger.info { "Checking local inference compatibility for model $modelId" }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val compatibility = mutableMapOf<String, Any>()
        
        // Check ONNX model availability
        val hasOnnxModel = try {
            storageService.readRawModelContentLength(model) > 0
        } catch (e: Exception) {
            false
        }
        
        // Check preprocessor availability
        val hasPreprocessor = try {
            storageService.readRawPreprocessor(model) != null
        } catch (e: Exception) {
            false
        }
        
        // Check model type support
        val supportedTypes = listOf("SKLEARN_ONNX", "TORCH_ONNX", "TORCH_SEQUENCE_ONNX", "TORCH_GEOMETRIC_ONNX", "TORCHSCRIPT")
        val isTypeSupported = model.type?.name in supportedTypes
        
        compatibility["modelId"] = modelId
        compatibility["hasOnnxModel"] = hasOnnxModel
        compatibility["hasPreprocessor"] = hasPreprocessor
        compatibility["modelType"] = model.type?.name ?: "UNKNOWN"
        compatibility["isTypeSupported"] = isTypeSupported
        compatibility["independentFeatures"] = model.independentFeatures.size
        compatibility["dependentFeatures"] = model.dependentFeatures.size
        compatibility["isCompatible"] = hasOnnxModel && isTypeSupported
        
        if (hasOnnxModel) {
            try {
                val modelSize = storageService.readRawModelContentLength(model)
                compatibility["modelSizeBytes"] = modelSize
                compatibility["modelSizeMB"] = modelSize / 1024.0 / 1024.0
            } catch (e: Exception) {
                logger.warn(e) { "Could not determine model size for model $modelId" }
            }
        }
        
        logger.info { "Compatibility check for model $modelId: compatible=${compatibility["isCompatible"]}" }
        
        return ResponseEntity.ok(compatibility)
    }

    /**
     * Download model file directly from database (local profile only).
     * 
     * This endpoint serves model files directly from the database for local development
     * when the local profile is active and the model is stored in the database.
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 30, intervalInSeconds = 60)
    fun downloadModelFromDatabase(modelId: Long): ResponseEntity<ByteArrayResource> {
        logger.info { "Direct model download request for model $modelId (profile: ${environment.activeProfiles.contentToString()})" }
        
        val isLocalProfile = environment.activeProfiles.contains("local")
        if (!isLocalProfile) {
            logger.warn { "Direct model download attempted outside local profile for model $modelId" }
            throw ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Direct database download only available in local profile")
        }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        if (model.rawModel == null) {
            logger.warn { "Model $modelId has no raw model data stored in database" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model data not stored in database")
        }

        try {
            val modelBytes = Base64.getDecoder().decode(model.rawModel)
            val resource = ByteArrayResource(modelBytes)
            
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_OCTET_STREAM
                setContentDispositionFormData("attachment", "model_${modelId}.onnx")
                contentLength = modelBytes.size.toLong()
            }
            
            logger.info { "Successfully serving model $modelId from database (${modelBytes.size} bytes)" }
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource)
                
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode or serve model $modelId from database" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serve model from database")
        }
    }

    /**
     * Download preprocessor file directly from database (local profile only).
     * 
     * This endpoint serves preprocessor files directly from the database for local development
     * when the local profile is active and the preprocessor is stored in the database.
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 30, intervalInSeconds = 60)
    fun downloadPreprocessorFromDatabase(modelId: Long): ResponseEntity<ByteArrayResource> {
        logger.info { "Direct preprocessor download request for model $modelId (profile: ${environment.activeProfiles.contentToString()})" }
        
        val isLocalProfile = environment.activeProfiles.contains("local")
        if (!isLocalProfile) {
            logger.warn { "Direct preprocessor download attempted outside local profile for model $modelId" }
            throw ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Direct database download only available in local profile")
        }
        
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        if (model.rawPreprocessor == null) {
            logger.warn { "Model $modelId has no preprocessor data stored in database" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor data not stored in database")
        }

        try {
            val preprocessorBytes = Base64.getDecoder().decode(model.rawPreprocessor)
            val resource = ByteArrayResource(preprocessorBytes)
            
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_OCTET_STREAM
                setContentDispositionFormData("attachment", "preprocessor_${modelId}.pkl")
                contentLength = preprocessorBytes.size.toLong()
            }
            
            logger.info { "Successfully serving preprocessor for model $modelId from database (${preprocessorBytes.size} bytes)" }
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource)
                
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode or serve preprocessor for model $modelId from database" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serve preprocessor from database")
        }
    }
}