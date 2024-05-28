package org.jaqpot.api.service.model

import org.jaqpot.api.ModelApiDelegate
import org.jaqpot.api.entity.Model
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.repository.ModelRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI


@Service
class ModelService(
    private val modelRepository: ModelRepository
) : ModelApiDelegate {
    override fun createModel(modelDto: ModelDto): ResponseEntity<Unit> {
        val model = modelRepository.save(modelDto.toEntity())
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(model.id).toUri()
        return ResponseEntity.created(location).build()
    }

    override fun getModels(): ResponseEntity<List<ModelDto>> {
        val models = modelRepository.findAll()
        return ResponseEntity.ok().body(models.map(Model::toDto))
    }

}

