package org.jaqpot.api.service.dataset

import org.jaqpot.api.DatasetApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toGetDatasets200ResponseDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.GetDatasets200ResponseDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Service

@Service
class DatasetService(private val datasetRepository: DatasetRepository, private val authenticationFacade: AuthenticationFacade) : DatasetApiDelegate {

    @PostAuthorize("@getDatasetAuthorizationLogic.decide(#root)")
    override fun getDatasetById(id: Long): ResponseEntity<DatasetDto> {
        val dataset = datasetRepository.findById(id)

        return dataset.map {
            ResponseEntity.ok(it.toDto())
        }
            .orElse(ResponseEntity.notFound().build())
    }

    override fun getDatasets(page: kotlin.Int,
                             size: kotlin.Int): ResponseEntity<GetDatasets200ResponseDto> {
        val userId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size)
        val datasets = datasetRepository.findAllByUserId(userId, pageable)

        return ResponseEntity.ok().body(datasets.toGetDatasets200ResponseDto())
    }
}
