package org.jaqpot.api.service.prediction.runtime

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelTypeDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionResponseDto
import org.jaqpot.api.service.DatasetCachedPredictionService
import org.jaqpot.api.service.model.QSARToolboxPredictionService
import org.jaqpot.api.service.prediction.runtime.runtimes.RESTRuntime
import org.jaqpot.api.service.prediction.runtime.runtimes.legacy.*
import org.jaqpot.api.service.prediction.runtime.runtimes.rest.JaqpotDockerModelRuntime
import org.jaqpot.api.service.prediction.runtime.runtimes.rest.JaqpotPyV6Runtime
import org.jaqpot.api.service.prediction.runtime.runtimes.rest.JaqpotRV6Runtime
import org.springframework.stereotype.Component


@Component
class PredictionChain(
    private val jaqpotPyV6Runtime: JaqpotPyV6Runtime,
    private val jaqpotRV6Runtime: JaqpotRV6Runtime,
    private val legacyPythonGeneric1Runtime: LegacyPythonGeneric1Runtime,
    private val legacyPythonGeneric020Runtime: LegacyPythonGeneric020Runtime,
    private val legacyPythonGeneric022Runtime: LegacyPythonGeneric022Runtime,
    private val legacyPythonGeneric023Runtime: LegacyPythonGeneric023Runtime,
    private val legacyPythonGeneric024Runtime: LegacyPythonGeneric024Runtime,
    private val legacyJaqpotInferenceRuntime: LegacyJaqpotInferenceRuntime,
    private val dockerRuntime: JaqpotDockerModelRuntime,
    private val qsarToolboxPredictionService: QSARToolboxPredictionService,
    private val datasetCachedPredictionService: DatasetCachedPredictionService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getPredictionResults(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): PredictionResponseDto {
        if (predictionModelDto.supportsPredictionCaching == true) {
            val result = datasetCachedPredictionService.getPredictionResults(predictionModelDto, datasetDto)
            if (result.isPresent) {
                return result.get()
            }
        }

        if (predictionModelDto.isQsarModel()) {
            val predictions = qsarToolboxPredictionService.makePredictionRequest(
                predictionModelDto,
                datasetDto,
                predictionModelDto.type
            )
            return PredictionResponseDto(predictions as List<Map<String, Any>>)
        }

        if (predictionModelDto.isRModel()) {
            return jaqpotRV6Runtime.sendPredictionRequest(predictionModelDto, datasetDto)
                .orElseThrow { JaqpotRuntimeException("Failed to succeed on the latest R runtime, modelId: ${predictionModelDto.id}") }
        }

        if (predictionModelDto.isDockerModel()) {
            return dockerRuntime.sendPredictionRequest(predictionModelDto, datasetDto)
                .orElseThrow { JaqpotRuntimeException("Failed to succeed on the Docker runtime, modelId: ${predictionModelDto.id}") }
        }

        if (!predictionModelDto.isLegacyModel()) {
            return jaqpotPyV6Runtime.sendPredictionRequest(predictionModelDto, datasetDto)
                .orElseThrow { JaqpotRuntimeException("Failed to succeed on the latest Python runtime, modelId: ${predictionModelDto.id}") }
        }

        val matchedLegacyRuntime =
            resolveRuntimeWithPredictionService(predictionModelDto.legacyPredictionService!!)

        return matchedLegacyRuntime.sendPredictionRequest(predictionModelDto, datasetDto)
            .or { legacyPythonGeneric020Runtime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .or { legacyPythonGeneric022Runtime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .or { legacyPythonGeneric023Runtime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .or { legacyPythonGeneric024Runtime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .or { legacyPythonGeneric1Runtime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .or { legacyJaqpotInferenceRuntime.sendPredictionRequest(predictionModelDto, datasetDto) }
            .orElseThrow { JaqpotRuntimeException("Failed to succeed on all the legacy runtimes for model ${predictionModelDto.id}") }
    }

    private fun resolveRuntimeWithPredictionService(legacyPredictionService: String): RESTRuntime {
        if (legacyPredictionService.contains("jaqpot-inference.jaqpot")) {
            return legacyJaqpotInferenceRuntime
        } else if (legacyPredictionService.contains("jaqpot-python")) {
            return if (legacyPredictionService.contains("jaqpot-python-generic-service.jaqpot")) {
                logger.info { "Using legacyPythonGeneric20 runtime" }
                legacyPythonGeneric020Runtime
            } else if (legacyPredictionService.contains("python-generic-service-22")) {
                logger.info { "Using legacyPythonGeneric22 runtime" }
                legacyPythonGeneric022Runtime
            } else if (legacyPredictionService.contains("python-generic-service-23")) {
                logger.info { "Using legacyPythonGeneric23 runtime" }
                legacyPythonGeneric023Runtime
            } else if (legacyPredictionService.contains("python-generic-service-24")) {
                legacyPythonGeneric024Runtime
            } else if (legacyPredictionService.contains("python-generic-service-1")) {
                legacyPythonGeneric1Runtime
            } else {
                throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
            }
        } else {
            throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
        }
    }
}

private fun PredictionModelDto.isQsarModel(): Boolean = this.type in listOf(
    ModelTypeDto.QSAR_TOOLBOX_CALCULATOR,
    ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL,
    ModelTypeDto.QSAR_TOOLBOX_PROFILER
)

private fun PredictionModelDto.isLegacyModel(): Boolean {
    return this.legacyPredictionService != null
}

private fun PredictionModelDto.isRModel(): Boolean {
    return this.type.name.startsWith("R_")
}

private fun PredictionModelDto.isDockerModel(): Boolean {
    return this.type == ModelTypeDto.DOCKER
}
