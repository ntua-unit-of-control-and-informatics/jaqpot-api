package org.jaqpot.api.service.model

import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI


@Service
class ModelService(
    private val authenticationFacade: AuthenticationFacade,
    private val modelRepository: ModelRepository,
    private val userService: UserService,
    private val predictionService: PredictionService, private val datasetRepository: DatasetRepository
) : ModelApiDelegate {
    override fun createModel(modelDto: ModelDto): ResponseEntity<Unit> {
        if (modelDto.id != null) {
            throw IllegalStateException("ID should not be provided for resource creation.")
        }
        val creatorId = authenticationFacade.userId
        val model = modelRepository.save(modelDto.toEntity(creatorId))
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(model.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @PostAuthorize("@getModelAuthorizationLogic.decide(#root)")
    override fun getModelById(id: Long): ResponseEntity<ModelDto> {
        val model = modelRepository.findById(id)

        return model.map {
            val user = userService.getUserById(it.creatorId)
            ResponseEntity.ok(it.toDto(user))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    @PreAuthorize("@predictModelAuthorizationLogic.decide(#root, #modelId)")
    override fun predictWithModel(modelId: Long, datasetDto: DatasetDto): ResponseEntity<Unit> {
        if (datasetDto.type == DatasetDto.Type.PREDICTION) {
            val model = this.modelRepository.findByIdOrNull(modelId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            val userId = authenticationFacade.userId
            val dataset = this.datasetRepository.save(datasetDto.toEntity(model, userId))

            this.predictionService.executePredictionAndSaveResults(model, dataset)

            val location: URI = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/datasets/{id}")
                .buildAndExpand(dataset.id).toUri()
            return ResponseEntity.created(location).build()
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown dataset type", null)
    }
}

