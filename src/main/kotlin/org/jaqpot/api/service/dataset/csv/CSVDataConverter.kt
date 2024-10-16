package org.jaqpot.api.service.dataset.csv

import org.jaqpot.api.entity.Model
import org.jaqpot.api.service.model.JAQPOT_ROW_ID_KEY
import org.springframework.stereotype.Component

@Component
class CSVDataConverter {
    fun convertCsvContentToInput(model: Model, csvData: List<List<String>>): List<Any> {
        return csvData.mapIndexed { index, it ->
            val inputObject = HashMap<String, String>()
            it.forEachIndexed { idx, value ->
                inputObject[model.independentFeatures[idx].key] = value
                inputObject[JAQPOT_ROW_ID_KEY] = index.toString()
            }

            inputObject
        }
    }
}
