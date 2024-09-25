package org.jaqpot.api.service.model

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.qsartoolbox.QSARToolboxAPI
import org.springframework.stereotype.Service

@Service
class QSARToolboxPredictionService(private val qsarToolboxAPI: QSARToolboxAPI) {

    companion object {
        private const val SMILES_KEY = "smiles"
        private const val CALCULATOR_GUID_KEY = "calculatorGuid"
    }

    fun makePredictionRequest(modelDto: PredictionModelDto, datasetDto: DatasetDto): List<Any> {
        return datasetDto.input.map {
            val datasetInput = it as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val calculatorGuid = datasetInput[CALCULATOR_GUID_KEY] as String
            val searchSmilesResults = qsarToolboxAPI.searchSmiles(smiles)
            val chemId =
                (searchSmilesResults!!.find { it.CasSmilesRelation == "High" } ?: searchSmilesResults.first()) as String

            qsarToolboxAPI.calculateQsarProperties(chemId, calculatorGuid)!!
        }
    }

}

typealias DatasetInput = Map<String, String>
