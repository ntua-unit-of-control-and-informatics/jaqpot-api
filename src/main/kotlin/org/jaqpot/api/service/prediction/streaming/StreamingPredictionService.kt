package org.jaqpot.api.service.prediction.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toPredictionModelDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.dataset.DatasetService
import org.jaqpot.api.service.model.JAQPOT_ROW_ID_KEY
import org.jaqpot.api.service.model.dto.StreamPredictRequestDto
import org.jaqpot.api.service.prediction.runtime.runtimes.streaming.StreamingModelRuntime
import org.jaqpot.api.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import java.time.OffsetDateTime

@Service
class StreamingPredictionService(
    private val storageService: StorageService,
    private val datasetRepository: DatasetRepository,
    private val datasetService: DatasetService,
    private val modelRepository: ModelRepository,
    private val streamingRuntime: StreamingModelRuntime
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getStreamingPrediction(
        modelId: Long,
        datasetId: Long,
        streamPredictRequestDto: StreamPredictRequestDto
    ): Flux<String> {
        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        if (model.archived) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Model with id $modelId is archived")
        }


        val toEntity = datasetRepository.findByIdAndModelId(datasetId, modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset with id $datasetId not found")
        }

        toEntity.input = storageService.readRawDatasetInput(toEntity)
        toEntity.result = storageService.readRawDatasetResult(toEntity)

        toEntity.input!!.forEachIndexed { index, it: Any ->
            if (it is Map<*, *>)
                (it as MutableMap<String, String>)[JAQPOT_ROW_ID_KEY] = index.toString()
        }
        toEntity.input = (toEntity.input ?: emptyList()) + listOf(mapOf("prompt" to streamPredictRequestDto.prompt))

        val predictionModelDto = model.toPredictionModelDto(byteArrayOf(), emptyList(), byteArrayOf())
        updateDatasetToExecuting(toEntity)
        storageService.storeRawDataset(toEntity)

        val datasetDto = toEntity.toDto(toEntity.input!!, toEntity.result)

        var output = ""

        return streamingRuntime.sendStreamingPredictionRequest(predictionModelDto, datasetDto)
            .doOnSubscribe {
                logger.info { "Starting streaming request for model ${predictionModelDto.id}" }
            }
            .doOnNext { response ->
                logger.info { "Received chunk: $response" }
                output += " $response"
            }
            .doOnError { e ->
                logger.error(e) { "Stream error for model ${predictionModelDto.id}: ${e.message}" }
                storeDatasetFailure(toEntity, e, datasetRepository)
            }
            .doFinally { signal ->
                logger.info { "Stream finished with signal $signal for model ${predictionModelDto.id}" }
                storeDatasetSuccess(datasetDto.id!!, mapOf("output" to output))
            }
    }

    private fun updateDatasetToExecuting(
        dataset: Dataset,
    ) {
        dataset.status = DatasetStatus.EXECUTING
        dataset.executedAt = OffsetDateTime.now()
        datasetRepository.save(dataset)
    }

    private fun storeDatasetSuccess(datasetId: Long, result: Any) {
        val dataset = datasetRepository.findById(datasetId).orElseThrow {
            throw JaqpotRuntimeException("Dataset with id $datasetId not found")
        }
        val datasetInput = storageService.readRawDatasetInput(dataset)?.toMutableList()
        dataset.input = datasetInput ?: emptyList()

        var datasetResult = storageService.readRawDatasetResult(dataset)?.toMutableList()
        if (datasetResult == null) {
            datasetResult = mutableListOf()
        }
        dataset.result = datasetResult + listOf(result)
        dataset.status = DatasetStatus.SUCCESS
        dataset.executionFinishedAt = OffsetDateTime.now()
        datasetRepository.save(dataset)
        if (storageService.storeRawDataset(dataset)) {
            datasetRepository.setDatasetInputAndResultToNull(dataset.id)
        }
    }


    private fun storeDatasetFailure(
        dataset: Dataset,
        err: Throwable,
        datasetRepository: DatasetRepository,
    ) {
        dataset.status = DatasetStatus.FAILURE
        dataset.failureReason = err.toString()
        dataset.executionFinishedAt = OffsetDateTime.now()

        datasetRepository.save(dataset)
    }
}
