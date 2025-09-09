package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.ModelDownloadApiDelegate
import org.jaqpot.api.model.GetModelDownloadUrls200ResponseDoaUrlsInnerDto
import org.jaqpot.api.model.GetModelDownloadUrls200ResponseDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.jaqpot.api.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
class ModelDownloadService(
    private val modelRepository: ModelRepository,
    private val storageService: StorageService,
) : ModelDownloadApiDelegate {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Get all presigned download URLs for model assets (model, preprocessor, DOAs).
     *
     * This endpoint enables comprehensive model download by providing secure, time-limited
     * download URLs for all available model assets stored in S3.
     *
     * @param modelId The ID of the model to get download URLs for
     * @param expirationMinutes URL expiration time in minutes (default 10, max 60)
     * @return Presigned download URLs for all available model assets
     */
    @PreAuthorize("@downloadModelAuthorizationLogic.decide(#root, #modelId)")
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 3600)
    override fun getModelDownloadUrls(
        modelId: Long,
        expirationMinutes: Int
    ): ResponseEntity<GetModelDownloadUrls200ResponseDto> {
        logger.info { "Generating S3 presigned download URLs for model $modelId" }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 10)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())

        // Generate model download URL
        var modelUrl: URI? = null
        try {
            val downloadUrl = storageService.getPreSignedModelDownloadUrl(model, effectiveExpirationMinutes)
            modelUrl = URI(downloadUrl)
            logger.debug { "Generated S3 presigned model URL for model $modelId" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to generate S3 presigned model download URL for model $modelId" }
        }

        // Generate preprocessor download URL
        var preprocessorUrl: URI? = null
        try {
            val preprocessor = storageService.readRawPreprocessor(model)
            if (preprocessor != null) {
                val downloadUrl = storageService.getPreSignedPreprocessorDownloadUrl(model, effectiveExpirationMinutes)
                preprocessorUrl = URI(downloadUrl)
                logger.debug { "Generated S3 presigned preprocessor URL for model $modelId" }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to generate S3 presigned preprocessor download URL for model $modelId" }
        }

        // Generate DOA download URLs
        val doaUrls = mutableListOf<GetModelDownloadUrls200ResponseDoaUrlsInnerDto>()
        for (doa in model.doas) {
            try {
                val doaRaw = storageService.readRawDoa(doa)
                if (doaRaw.isNotEmpty()) {
                    val downloadUrl = storageService.getPreSignedDoaDownloadUrl(doa, effectiveExpirationMinutes)
                    doaUrls.add(
                        GetModelDownloadUrls200ResponseDoaUrlsInnerDto(
                            method = doa.method.name,
                            downloadUrl = URI(downloadUrl)
                        )
                    )
                    logger.debug { "Generated S3 presigned DOA URL for model $modelId, method ${doa.method.name}" }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to generate S3 presigned DOA download URL for model $modelId, method ${doa.method.name}" }
            }
        }

        logger.info { "Successfully generated S3 presigned URLs for model $modelId: model=${modelUrl != null}, preprocessor=${preprocessorUrl != null}, doas=${doaUrls.size}" }

        return ResponseEntity.ok(
            GetModelDownloadUrls200ResponseDto(
                modelUrl = modelUrl,
                preprocessorUrl = preprocessorUrl,
                doaUrls = doaUrls.ifEmpty { null },
                expiresAt = expiresAt
            )
        )
    }


}
