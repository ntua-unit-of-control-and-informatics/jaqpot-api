package org.jaqpot.api.service.prediction.streaming

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.service.prediction.runtime.runtimes.streaming.StreamingRuntime
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class StreamingPredictionService(private val streamingRuntime: StreamingRuntime) {
    fun getStreamingPrediction(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): Flux<String> {
        return streamingRuntime.sendStreamingPredictionRequest(predictionModelDto, datasetDto)
    }
}
