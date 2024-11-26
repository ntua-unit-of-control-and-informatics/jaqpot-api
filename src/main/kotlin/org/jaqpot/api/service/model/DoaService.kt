package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Doa
import org.jaqpot.api.repository.DoaRepository
import org.jaqpot.api.storage.StorageService
import org.springframework.stereotype.Service

@Service
class DoaService(private val doaRepository: DoaRepository, private val storageService: StorageService) {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    fun storeRawDoaToStorage(doa: Doa) {
        if (doa.rawDoa == null) {
            // doa is already stored in storage
            return
        }
        logger.info { "Storing raw doa to storage for doa with id ${doa.id} and model ${doa.model.id}" }
        if (storageService.storeRawDoa(doa)) {
            logger.info { "Successfully moved raw doa to storage for doa ${doa.id} and model ${doa.model.id}" }
            doaRepository.setRawDoaToNull(doa.id)
        }
    }
}
