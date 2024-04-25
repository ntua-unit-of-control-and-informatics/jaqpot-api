package org.jaqpot.api.controller


import org.jaqpot.api.entity.Model
import org.jaqpot.api.repository.ModelRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ModelController(private val modelRepository: ModelRepository) {

    @GetMapping("/models")
    fun findAllModels(): Iterable<Model> {
        return modelRepository.findAll()
    }

    @PostMapping("/models")
    fun createModel(@RequestBody model: Model): Model {
        return modelRepository.save(model)
    }
}
