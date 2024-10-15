package org.jaqpot.api.service.dataset

import org.jaqpot.api.DatasetApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toGetDatasets200ResponseDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.GetDatasets200ResponseDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.util.SortUtil.Companion.parseSortParameters
import org.jaqpot.api.storage.StorageService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Service

@Service
class DatasetService(
    private val datasetRepository: DatasetRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val storageService: StorageService
) : DatasetApiDelegate {

    @PostAuthorize("@getDatasetAuthorizationLogic.decide(#root)")
    override fun getDatasetById(id: Long): ResponseEntity<DatasetDto> {
        val dataset = datasetRepository.findById(id)

        return dataset.map {
            val input = storageService.readRawDatasetInput(it)
            val result = storageService.readRawDatasetResult(it)
            ResponseEntity.ok(it.toDto(input, result))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    override fun getDatasets(page: Int, size: Int, sort: List<String>?): ResponseEntity<GetDatasets200ResponseDto> {
        val userId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))
        val datasets = datasetRepository.findAllByUserId(userId, pageable)

        val inputsMap = storageService.readRawDatasetInputs(datasets.content)
        val resultsMap = storageService.readRawDatasetResults(datasets.content)

        return ResponseEntity.ok().body(datasets.toGetDatasets200ResponseDto(inputsMap, resultsMap))
    }
}
