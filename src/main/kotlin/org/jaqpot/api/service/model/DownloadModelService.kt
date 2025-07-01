package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.ModelDownloadApiDelegate
import org.jaqpot.api.model.GetModelDownloadUrl200ResponseDto
import org.jaqpot.api.model.GetModelPreprocessorDownloadUrl200ResponseDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.jaqpot.api.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Service for model download operations.
 *
 * This service provides functionality for downloading models and preprocessors
 * for local development and testing using S3 presigned URLs only.
 *
 * Separated from ModelService to keep concerns focused:
 * - ModelService: Core model CRUD operations
 * - DownloadModelService: Model download support (S3 presigned URLs)
 */
@Service
class DownloadModelService(
    private val modelRepository: ModelRepository,
    private val storageService: StorageService,
) : ModelDownloadApiDelegate {


    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Get a presigned URL to download the ONNX model file for local inference.
     *
     * This endpoint enables local model development by providing secure, time-limited
     * download URLs for model files stored in S3.
     *
     * @param modelId The ID of the model to get download URL for
     * @param expirationMinutes URL expiration time in minutes (default 10, max 60)
     * @return Presigned download URL with expiration timestamp
     */
    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    @WithRateLimitProtectionByUser(limit = 30, intervalInSeconds = 60)
    override fun getModelDownloadUrl(
        modelId: Long,
        expirationMinutes: Int
    ): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
        logger.info { "Generating S3 presigned download URL for model $modelId" }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())

        try {
            val downloadUrl = storageService.getPreSignedModelDownloadUrl(model, effectiveExpirationMinutes)

            logger.info { "Successfully generated S3 presigned URL for model $modelId, expires at $expiresAt" }

            return ResponseEntity.ok(
                GetModelDownloadUrl200ResponseDto(
                    downloadUrl = URI(downloadUrl),
                    expiresAt = expiresAt
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate S3 presigned download URL for model $modelId" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model file not found in storage")
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
    override fun getModelPreprocessorDownloadUrl(
        modelId: Long,
        expirationMinutes: Int
    ): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto> {
        logger.info { "Generating S3 presigned preprocessor download URL for model $modelId" }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())

        try {
            val preprocessor = storageService.readRawPreprocessor(model)
            if (preprocessor == null) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor not found for model $modelId")
            }

            val downloadUrl = storageService.getPreSignedPreprocessorDownloadUrl(model, effectiveExpirationMinutes)

            logger.info { "Successfully generated S3 presigned preprocessor URL for model $modelId, expires at $expiresAt" }

            return ResponseEntity.ok(
                GetModelPreprocessorDownloadUrl200ResponseDto(
                    downloadUrl = URI(downloadUrl),
                    expiresAt = expiresAt
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate S3 presigned preprocessor download URL for model $modelId" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor not found for model $modelId")
        }
    }


}
