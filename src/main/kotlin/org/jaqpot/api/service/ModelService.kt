package org.jaqpot.api.service

import org.jaqpot.openapi.ModelApiDelegate
import org.jaqpot.openapi.model.ModelDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ModelService : ModelApiDelegate {
    override fun getModels(): ResponseEntity<ModelDto> {
        return ResponseEntity(ModelDto(1, true), HttpStatus.OK)
    }
}

