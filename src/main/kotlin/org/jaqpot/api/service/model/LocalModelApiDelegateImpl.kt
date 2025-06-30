package org.jaqpot.api.service.model

import org.jaqpot.api.LocalModelApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.CheckLocalInferenceCompatibility200ResponseDto
import org.jaqpot.api.model.GetModelDownloadUrl200ResponseDto
import org.jaqpot.api.model.GetModelPreprocessorDownloadUrl200ResponseDto
import org.jaqpot.api.model.ModelDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

/**
 * Implementation of LocalModelApiDelegate following the OpenAPI-first architecture.
 * 
 * This service delegates LocalModel API operations to the LocalModelService,
 * maintaining consistency with the established pattern where:
 * - Controllers are auto-generated from OpenAPI specs
 * - Business logic is implemented in Service classes
 * - API delegates bridge the gap between generated controllers and service implementations
 * 
 * Note: This implementation currently supports the endpoints defined in the OpenAPI spec.
 * The direct database download endpoints (for local profile) that were in the manual controller
 * are not yet included in the OpenAPI spec and thus not available through this delegate.
 * These endpoints (/v1/models/{modelId}/download/local and /v1/models/{modelId}/preprocessor/download/local)
 * remain accessible through the LocalModelService if needed in the future.
 */
@Service
class LocalModelApiDelegateImpl : LocalModelApiDelegate {

    @Autowired
    private lateinit var localModelService: LocalModelService

    override fun checkLocalInferenceCompatibility(modelId: kotlin.Long): ResponseEntity<CheckLocalInferenceCompatibility200ResponseDto> {
        val result = localModelService.checkLocalInferenceCompatibility(modelId)
        
        // Convert from generic Map<String, Any> to specific DTO
        val compatibility = result.body?.let { data ->
            CheckLocalInferenceCompatibility200ResponseDto(
                modelId = data["modelId"] as Long,
                hasOnnxModel = data["hasOnnxModel"] as Boolean,
                hasPreprocessor = data["hasPreprocessor"] as Boolean,
                modelType = data["modelType"] as String,
                isTypeSupported = data["isTypeSupported"] as Boolean,
                independentFeatures = data["independentFeatures"] as Int,
                dependentFeatures = data["dependentFeatures"] as Int,
                isCompatible = data["isCompatible"] as Boolean,
                modelSizeBytes = (data["modelSizeBytes"] as Long?)?.toInt(),
                modelSizeMB = (data["modelSizeMB"] as Double?)?.toFloat()
            )
        }
        
        return ResponseEntity.status(result.statusCode).body(compatibility)
    }

    override fun getModelDownloadUrl(
        modelId: kotlin.Long,
        expirationMinutes: kotlin.Int
    ): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
        return localModelService.getModelDownloadUrl(modelId, expirationMinutes)
    }

    override fun getModelMetadataForLocal(modelId: kotlin.Long): ResponseEntity<ModelDto> {
        val result = localModelService.getModelMetadataForLocal(modelId)
        
        // Convert Model entity to ModelDto using the mapper
        val modelDto = result.body?.toDto()
        
        return ResponseEntity.status(result.statusCode).body(modelDto)
    }

    override fun getModelPreprocessorDownloadUrl(
        modelId: kotlin.Long,
        expirationMinutes: kotlin.Int
    ): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto> {
        return localModelService.getModelPreprocessorDownloadUrl(modelId, expirationMinutes)
    }
}