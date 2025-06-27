package org.jaqpot.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.GetModelDownloadUrl200ResponseDto
import org.jaqpot.api.model.GetModelPreprocessorDownloadUrl200ResponseDto
import org.jaqpot.api.service.model.LocalModelService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for local model development operations.
 * 
 * Provides endpoints for downloading models and preprocessors for local development,
 * separate from the main model management operations.
 */
@RestController
@RequestMapping("/v1/models")
@Tag(name = "local-model", description = "Local model development operations")
@SecurityRequirement(name = "bearerAuth")
class LocalModelController {

    @Autowired
    private lateinit var localModelService: LocalModelService

    @Operation(
        summary = "Get presigned URL for model download", 
        description = "Generate a presigned URL to download the ONNX model file for local inference development"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Presigned download URL generated successfully"),
            ApiResponse(responseCode = "404", description = "Model not found or not stored in S3"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/{modelId}/local/download-url")
    fun getModelDownloadUrl(
        @Parameter(description = "The ID of the model to get download URL for", required = true)
        @PathVariable modelId: Long,
        
        @Parameter(description = "URL expiration time in minutes (default 10, max 60)")
        @RequestParam(defaultValue = "10") expirationMinutes: Int
    ): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
        return localModelService.getModelDownloadUrl(modelId, expirationMinutes)
    }

    @Operation(
        summary = "Get presigned URL for preprocessor download",
        description = "Generate a presigned URL to download the preprocessor file for local inference development"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Presigned download URL generated successfully"),
            ApiResponse(responseCode = "404", description = "Model or preprocessor not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/{modelId}/local/preprocessor/download-url")
    fun getModelPreprocessorDownloadUrl(
        @Parameter(description = "The ID of the model to get preprocessor download URL for", required = true)
        @PathVariable modelId: Long,
        
        @Parameter(description = "URL expiration time in minutes (default 10, max 60)")
        @RequestParam(defaultValue = "10") expirationMinutes: Int
    ): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto> {
        return localModelService.getModelPreprocessorDownloadUrl(modelId, expirationMinutes)
    }

    @Operation(
        summary = "Get model metadata for local development",
        description = "Retrieve essential model information needed for local inference setup"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Model metadata retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Model not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    @GetMapping("/{modelId}/local/metadata")
    fun getModelMetadataForLocal(
        @Parameter(description = "The ID of the model to get metadata for", required = true)
        @PathVariable modelId: Long
    ): ResponseEntity<Model> {
        return localModelService.getModelMetadataForLocal(modelId)
    }

    @Operation(
        summary = "Check local inference compatibility",
        description = "Validate that a model has all necessary components for local development"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Compatibility information retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Model not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    @GetMapping("/{modelId}/local/compatibility")
    fun checkLocalInferenceCompatibility(
        @Parameter(description = "The ID of the model to check compatibility for", required = true)
        @PathVariable modelId: Long
    ): ResponseEntity<Map<String, Any>> {
        return localModelService.checkLocalInferenceCompatibility(modelId)
    }
}