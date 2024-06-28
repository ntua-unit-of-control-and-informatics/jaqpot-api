package org.jaqpot.api.service.dataset.csv

import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DataEntryDto
import org.springframework.stereotype.Component

@Component
class CSVDataConverter {
    fun convertCsvContentToDataEntry(model: Model, csvData: List<List<String>>): DataEntryDto {
        val propertyValues = csvData.map {
            it.mapIndexed { idx, value ->
                {
                    val inputObject = HashMap<String, String>()

                    inputObject.put(model.independentFeatures[idx].key, value)
                }
            }
        }

        return DataEntryDto(DataEntryDto.Type.ARRAY, propertyValues)
    }
}
