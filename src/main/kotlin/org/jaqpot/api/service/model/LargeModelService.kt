package org.jaqpot.api.service.model

import org.jaqpot.api.LargeModelApi
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.CreateLargeModel201ResponseDto
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.model.ModelService.Companion.FORBIDDEN_MODEL_TYPES_FOR_CREATION
import org.jaqpot.api.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
class LargeModelService(
    private val authenticationFacade: AuthenticationFacade,
    private val modelRepository: ModelRepository,
    private val doaService: DoaService,
    private val storageService: StorageService
) : LargeModelApi {

    companion object {
        const val MAX_MODEL_SIZE_BYTES = 100 * 1024 * 1024 // 100 MB
    }

    override fun createLargeModel(
        modelDto: ModelDto
    ): ResponseEntity<CreateLargeModel201ResponseDto> {
        if (modelDto.id != null) {
            throw IllegalStateException("ID should not be provided for resource creation.")
        }

        if (FORBIDDEN_MODEL_TYPES_FOR_CREATION.contains(modelDto.type) && !authenticationFacade.isAdmin && !authenticationFacade.isUpciUser) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "${modelDto.type} is not supported for creation.")
        }

        if (modelDto.rawModel != null) {
            throw IllegalArgumentException("rawModel must be omitted for large uploads.")
        }

        val creatorId = authenticationFacade.userId
        val toEntity = modelDto.toEntity(creatorId)
        val savedModel = modelRepository.save(toEntity)
        ModelService.storeRawModelToStorage(savedModel, storageService, modelRepository)
        ModelService.storeRawPreprocessorToStorage(savedModel, storageService, modelRepository)
        savedModel.doas.forEach(doaService::storeRawDoaToStorage)
        toEntity.uploadConfirmed = false

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(savedModel.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @PreAuthorize("@largeModelConfirmUploadAuthorizationLogic.decide(#root, #modelId)")
    override fun confirmLargeModelUpload(modelId: String): ResponseEntity<Unit> {
        val model = modelRepository.findById(modelId.toLong())
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found") }

        if (model.uploadConfirmed) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Upload already confirmed")
        }

        try {
            val objectMeta = storageService.readRawModelMetadata(model)
            if (objectMeta.contentLength() > MAX_MODEL_SIZE_BYTES) {
                storageService.deleteRawModel(model)
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded model exceeds 100MB and has been removed"
                )
            }
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Model file not found in storage")
        }

        model.uploadConfirmed = true
        modelRepository.save(model)

        return ResponseEntity.noContent().build()
    }

}
