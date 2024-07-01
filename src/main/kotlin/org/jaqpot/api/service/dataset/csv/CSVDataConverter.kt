package org.jaqpot.api.service.dataset.csv

import org.jaqpot.api.entity.Model
import org.springframework.stereotype.Component

@Component
class CSVDataConverter {
    fun convertCsvContentToDataEntry(model: Model, csvData: List<List<String>>): List<Any> {
        return csvData.map {
            val inputObject = HashMap<String, String>()
            it.forEachIndexed { idx, value ->
                inputObject[model.independentFeatures[idx].key] = value
            }

            inputObject
        }
    }
}
