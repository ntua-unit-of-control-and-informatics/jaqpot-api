package org.jaqpot.api.service.model

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.qsartoolbox.QSARToolbox
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service

@Service
class QSARToolboxPredictionService(private val qsarToolbox: QSARToolbox) {

    companion object {
        private const val SMILES_KEY = "smiles"
        private const val CALCULATOR_GUID_KEY = "calculatorGuid"
    }

    fun makePredictionRequest(modelDto: PredictionModelDto, request: HttpEntity<PredictionRequestDto>): List<Any> {
        return request.body.dataset.input.map {
            val datasetInput = it as DatasetInput
            val smiles = datasetInput[SMILES_KEY] as String
            val calculatorGuid = datasetInput[CALCULATOR_GUID_KEY] as String
            val searchSmiles = qsarToolbox.searchSmiles(smiles)
            val chemId = searchSmiles!![0].ChemId as String
            qsarToolbox.calculateQsarProperties(chemId, calculatorGuid)!!
        }
    }

}

typealias DatasetInput = Map<String, String>
