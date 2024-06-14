package org.jaqpot.api.service.dataset

import org.jaqpot.api.DatasetApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.repository.DatasetRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DatasetService(private val datasetRepository: DatasetRepository) : DatasetApiDelegate {
    // TODO only allow access to the user that created the dataset
    override fun getDatasetById(id: Long): ResponseEntity<DatasetDto> {
        val dataset = datasetRepository.findById(id)

        return dataset.map {
            ResponseEntity.ok(it.toDto())
        }
            .orElse(ResponseEntity.notFound().build())
    }
}
