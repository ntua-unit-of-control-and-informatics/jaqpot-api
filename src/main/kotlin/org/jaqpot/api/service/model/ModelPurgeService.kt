package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.jaqpot.api.entity.Model
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.storage.StorageService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ModelPurgeService(
    private val storageService: StorageService,
    private val modelRepository: ModelRepository
) {

    companion object {
        const val ARCHIVED_MODEL_EXPIRATION_DAYS = 30L
        const val UNCONFIRMED_UPLOAD_MODEL_EXPIRATION_DAYS = 30L
        private val logger = KotlinLogging.logger {}
    }

    private fun deleteModel(model: Model) {
        logger.info { "Deleting model with id ${model.id} and creator id: ${model.creatorId}" }

        if (model.doas.isNotEmpty()) {
            model.doas.forEach {
                logger.info { "Deleting DOA with id ${it.id} for model with id ${model.id}" }
                val deletedRawDoa = storageService.deleteRawDoa(it)
                logger.info { "Deleted raw DOA for model with id ${model.id}: $deletedRawDoa" }
            }
        }

        logger.info { "Deleting raw preprocessor for model with id ${model.id}" }
        val deletedRawPreprocessor = storageService.deleteRawPreprocessor(model)
        logger.info { "Deleted raw preprocessor for model with id ${model.id}: $deletedRawPreprocessor" }

        logger.info { "Deleting raw model for model with id ${model.id}" }
        val deletedRawModel = storageService.deleteRawModel(model)
        logger.info { "Deleted raw model for model with id ${model.id}: $deletedRawModel" }

        modelRepository.delete(model)

        logger.info { "Deleted model with id ${model.id}" }
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *" /* every day at 3:00 AM */)
    fun purgeExpiredArchivedModels() {
        logger.info { "Purging expired archived models" }

        val expiredArchivedModels = modelRepository.findAllByArchivedIsTrueAndArchivedAtBefore(
            OffsetDateTime.now().minusDays(ARCHIVED_MODEL_EXPIRATION_DAYS)
        )

        var deletionCount = 0

        expiredArchivedModels.forEach {
            try {
                this.deleteModel(it)
                deletionCount++
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete model with id ${it.id}" }
            }
        }

        logger.info { "Purged $deletionCount expired archived models" }
    }

    @Transactional
    @Scheduled(cron = "0 0 4 * * *" /* every day at 4:00 AM */)
    fun purgeExpiredUnconfirmedUploadModels() {
        logger.info { "Purging expired unconfirmed upload models models" }

        val expiredArchivedModels = modelRepository.findAllByUploadConfirmedIsFalseAndCreatedAtBefore(
            OffsetDateTime.now().minusDays(UNCONFIRMED_UPLOAD_MODEL_EXPIRATION_DAYS)
        )

        var deletionCount = 0

        expiredArchivedModels.forEach {
            try {
                logger.info { "Deleting unconfirmed upload model with id ${it.id}" }
                // TODO uncomment next line once we're sure this works correctly
//                this.deleteModel(it)
                deletionCount++
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete model with id ${it.id}" }
            }
        }

        logger.info { "Purged $deletionCount expired unconfirmed upload models" }
    }
}
