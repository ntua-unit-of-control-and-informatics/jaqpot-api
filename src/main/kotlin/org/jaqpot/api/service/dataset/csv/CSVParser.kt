package org.jaqpot.api.service.dataset.csv

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.jaqpot.api.model.DataEntryDto
import org.springframework.stereotype.Component
import java.io.InputStream

private val logger = KotlinLogging.logger {}

@Component
class CSVParser {

    private val csvParser = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
        setIgnoreSurroundingSpaces(true)
    }.build()

    fun readCsv(inputStream: InputStream): List<DataEntryDto> {
        try {

            val inputValues = csvParser.parse(inputStream.reader())
                .drop(1) // Dropping the header
                .map {
                    it.toList()
                }
            return listOf(
                DataEntryDto(
                    DataEntryDto.Type.ARRAY,
                    inputValues
                )
            )
        } catch (e: Exception) {
            logger.error { "Error while parsing CSV $e" }
            throw CSVParserException("Error while parsing CSV", e)
        }
    }
}
