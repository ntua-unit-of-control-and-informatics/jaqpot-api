package org.jaqpot.api.service.dataset.csv

import org.jaqpot.api.entity.Model
import org.springframework.stereotype.Component

@Component
class CSVDataConverter {
    fun convertCsvContentToDataEntry(model: Model, csvData: List<List<String>>): List<Any> {
        return csvData.map {
            it.mapIndexed { idx, value ->
                {
                    val inputObject = HashMap<String, String>()

                    inputObject.put(model.independentFeatures[idx].key, value)
                }
            }
        }
    }
}
