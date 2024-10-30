package org.jaqpot.api.service.model

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelTypeDto
import org.jaqpot.api.model.PredictionModelDto
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

    private fun makeQsarModelPredictionRequest(datasetDto: DatasetDto): List<Map<*, *>> {
        return datasetDto.input.flatMapIndexed { index, input ->
            val datasetInput = input as DatasetInput
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
                addJaqpotMetadata(qsarProperties, index, input)
                qsarProperties
            }
        }
    }

    private fun addJaqpotMetadata(
        qsarProperties: MutableMap<Any?, Any?>,
        index: Int,
        input: DatasetInput
    ) {
        val jaqpotMetadata = mutableMapOf<String, Any>()

        jaqpotMetadata[JAQPOT_ROW_ID_KEY] = index.toString()
        if (input[JAQPOT_ROW_LABEL_KEY] != null) {
            jaqpotMetadata[JAQPOT_ROW_LABEL_KEY] = input[JAQPOT_ROW_LABEL_KEY].toString()
        }

        qsarProperties[JAQPOT_METADATA_KEY] = jaqpotMetadata
    }

    private fun makeCalculatorPredictionRequest(datasetDto: DatasetDto): List<Map<*, *>> {
        return datasetDto.input.flatMapIndexed { index, input ->
            val datasetInput = input as DatasetInput
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
                addJaqpotMetadata(qsarProperties, index, input)
                qsarProperties
            }
        }
    }

    private fun makeProfilerPredictionRequest(
        datasetDto: DatasetDto
    ): List<Map<String, Any>> {
        return datasetDto.input.flatMapIndexed { index, input ->
            val datasetInput = input as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val profilerGuid = datasetInput[PROFILER_GUID_KEY] as String
            val searchSmilesResults = qsarToolboxAPI.searchSmiles(smiles)
            // we only use results that have high CAS relation with the input smiles
            var results =
                searchSmilesResults!!.filter { smilesResponse -> smilesResponse.CasSmilesRelation == "High" }
            if (results.isEmpty()) {
                results = listOf(searchSmilesResults.first())
            }

            results.flatMap { result ->
                qsarToolboxAPI.runProfiler(result.ChemId as String, profilerGuid)!!
                    .map { it ->
                        val qsarProperties = mutableMapOf<String, Any>()
                        qsarProperties["Value"] = it
                        qsarProperties["Name"] = result.Names.joinToString { it }
                        qsarProperties["ChemId"] = result.ChemId
                        addJaqpotMetadata(qsarProperties as MutableMap<Any?, Any?>, index, input)
                        qsarProperties
                    }

            }
        }
    }

    fun makePredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto,
        type: ModelTypeDto
    ): List<Map<*, *>> {
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

}

typealias DatasetInput = Map<String, String>
