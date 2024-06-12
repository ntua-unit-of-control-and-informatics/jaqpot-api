package org.jaqpot.api.service.model

import jakarta.transaction.Transactional
import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.entity.Model
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.mapper.toGetModels200ResponseDto
import org.jaqpot.api.model.*
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.springframework.data.domain.PageRequest
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
    private val predictionService: PredictionService,
    private val datasetRepository: DatasetRepository,
    private val organizationRepository: OrganizationRepository,
) : ModelApiDelegate {

    override fun getModels(page: Int, size: Int): ResponseEntity<GetModels200ResponseDto> {
        val creatorId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size)
        val modelsPage = modelRepository.findAllByCreatorId(creatorId, pageable)
        val creator = userService.getUserById(creatorId)

        return ResponseEntity.ok().body(modelsPage.toGetModels200ResponseDto(creator))
    }

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
            val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, it)
            val user = userService.getUserById(it.creatorId)
            ResponseEntity.ok(it.toDto(user, userCanEdit))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    private fun isCreator(authenticationFacade: AuthenticationFacade, model: Model): Boolean {
        return authenticationFacade.userId == model.creatorId
    }

    @PreAuthorize("@predictModelAuthorizationLogic.decide(#root, #modelId)")
    override fun predictWithModel(modelId: Long, datasetDto: DatasetDto): ResponseEntity<Unit> {
        if (datasetDto.type == DatasetDto.Type.PREDICTION) {
            val model = modelRepository.findById(modelId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
            }
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

    @PreAuthorize("@partialModelUpdateAuthorizationLogic.decide(#root, #id)")
    @Transactional
    override fun partiallyUpdateModel(
        id: Long,
        partiallyUpdateModelRequestDto: PartiallyUpdateModelRequestDto
    ): ResponseEntity<ModelDto> {
        val existingModel = modelRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $id not found")
        }
        partiallyUpdateModelRequestDto.name.let { existingModel.name = it }
        partiallyUpdateModelRequestDto.visibility.let { existingModel.visibility = it.toEntity() }
        if (partiallyUpdateModelRequestDto.visibility == ModelVisibilityDto.ORG_SHARED) {
            partiallyUpdateModelRequestDto.organizationIds?.let {
                val organizations = organizationRepository.findAllById(it)
                existingModel.organizations.clear()
                existingModel.organizations.addAll(organizations)
            }
        } else {
            existingModel.organizations.clear()
        }

        val model: Model = modelRepository.save(existingModel)

        val userCanEdit = authenticationFacade.isAdmin || isCreator(authenticationFacade, model)
        val user = userService.getUserById(model.creatorId)
        return ResponseEntity.ok(model.toDto(user, userCanEdit))
    }


}

