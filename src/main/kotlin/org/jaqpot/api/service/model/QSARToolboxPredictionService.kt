package org.jaqpot.api.service.model

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelTypeDto
import org.jaqpot.api.service.qsartoolbox.QSARToolboxAPI
import org.springframework.stereotype.Service

@Service
class QSARToolboxPredictionService(private val qsarToolboxAPI: QSARToolboxAPI) {

    companion object {
        private const val SMILES_KEY = "smiles"
        private const val CALCULATOR_GUID_KEY = "calculatorGuid"
        private const val QSAR_MODEL_GUID_KEY = "qsarGuid"
        private const val PROFILER_GUID_KEY = "profilerGuid"
    }

    private fun makeQsarModelPredictionRequest(datasetDto: DatasetDto): List<Any> {
        return datasetDto.input.flatMapIndexed { index, it ->
            val datasetInput = it as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val qsarGuid = datasetInput[QSAR_MODEL_GUID_KEY] as String
            val searchSmilesResults = qsarToolboxAPI.searchSmiles(smiles)
            // we only use results that have high CAS relation with the input smiles
            var results =
                searchSmilesResults!!.filter { smilesResponse -> smilesResponse.CasSmilesRelation == "High" }
            if (results.isEmpty()) {
                results = listOf(searchSmilesResults.first())
            }

            results.map { result ->
                val qsarProperties =
                    qsarToolboxAPI.runQsarModel(result.ChemId as String, qsarGuid)!!.toMutableMap()
                qsarProperties["Name"] = result.Names.joinToString { it }
                qsarProperties["ChemId"] = result.ChemId
                qsarProperties[JAQPOT_INTERNAL_ID_KEY] = index.toString()
                qsarProperties
            }
        }
    }

    private fun makeCalculatorPredictionRequest(datasetDto: DatasetDto): List<Any> {
        return datasetDto.input.flatMapIndexed { index, it ->
            val datasetInput = it as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val calculatorGuid = datasetInput[CALCULATOR_GUID_KEY] as String
            val searchSmilesResults = qsarToolboxAPI.searchSmiles(smiles)
            // we only use results that have high CAS relation with the input smiles
            var results =
                searchSmilesResults!!.filter { smilesResponse -> smilesResponse.CasSmilesRelation == "High" }
            if (results.isEmpty()) {
                results = listOf(searchSmilesResults.first())
            }

            results.map { result ->
                val qsarProperties =
                    qsarToolboxAPI.runCalculator(result.ChemId as String, calculatorGuid)!!.toMutableMap()
                qsarProperties["Name"] = result.Names.joinToString { it }
                qsarProperties["ChemId"] = result.ChemId
                qsarProperties[JAQPOT_INTERNAL_ID_KEY] = index.toString()
                qsarProperties
            }
        }
    }

    fun makePredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto,
        type: ModelTypeDto
    ): List<Any> {
        return when (type) {
            ModelTypeDto.QSAR_TOOLBOX_CALCULATOR -> {
                makeCalculatorPredictionRequest(datasetDto)
            }

            ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL -> {
                makeQsarModelPredictionRequest(datasetDto)
            }

            ModelTypeDto.QSAR_TOOLBOX_PROFILER -> {
                makeProfilerPredictionRequest(datasetDto)
            }

            else -> {
                throw IllegalArgumentException("Unsupported model type: $type")
            }
        }
    }

    private fun makeProfilerPredictionRequest(
        datasetDto: DatasetDto
    ): List<Any> {
        return datasetDto.input.flatMapIndexed { index, it ->
            val datasetInput = it as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val profilerGuid = datasetInput[PROFILER_GUID_KEY] as String
            val searchSmilesResults = qsarToolboxAPI.searchSmiles(smiles)
            // we only use results that have high CAS relation with the input smiles
            var results =
                searchSmilesResults!!.filter { smilesResponse -> smilesResponse.CasSmilesRelation == "High" }
            if (results.isEmpty()) {
                results = listOf(searchSmilesResults.first())
            }

            results.map { result ->
                val qsarProperties =
                    qsarToolboxAPI.runProfiler(result.ChemId as String, profilerGuid)!!.toMutableMap()
                qsarProperties["Name"] = result.Names.joinToString { it }
                qsarProperties["ChemId"] = result.ChemId
                qsarProperties[JAQPOT_INTERNAL_ID_KEY] = index.toString()
                qsarProperties
            }
        }
    }

}

typealias DatasetInput = Map<String, String>
