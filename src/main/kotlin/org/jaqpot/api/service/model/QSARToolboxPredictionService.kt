package org.jaqpot.api.service.model

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.qsartoolbox.QSARToolboxAPI
import org.jaqpot.api.service.qsartoolbox.dto.QSARSearchSmilesResponse
import org.springframework.stereotype.Service

@Service
class QSARToolboxPredictionService(private val qsarToolboxAPI: QSARToolboxAPI) {

    companion object {
        private const val SMILES_KEY = "smiles"
        private const val CALCULATOR_GUID_KEY = "calculatorGuid"
    }

    fun makePredictionRequest(modelDto: PredictionModelDto, datasetDto: DatasetDto): List<Any> {
        return datasetDto.input.flatMap {
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
                generateQsarResult(result, calculatorGuid)
            }
        }
    }

    private fun generateQsarResult(
        result: QSARSearchSmilesResponse,
        calculatorGuid: String
    ): Map<*, *> {
        val qsarProperties =
            qsarToolboxAPI.calculateQsarProperties(result.ChemId as String, calculatorGuid)!!.toMutableMap()
        qsarProperties["Name"] = result.Names.joinToString { it }
        qsarProperties["ChemId"] = result.ChemId
        return qsarProperties
    }

}

typealias DatasetInput = Map<String, String>
