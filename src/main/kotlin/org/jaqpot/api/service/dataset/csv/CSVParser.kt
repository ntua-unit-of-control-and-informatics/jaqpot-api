package org.jaqpot.api.service.dataset.csv

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.springframework.stereotype.Component
import java.io.InputStream

private val logger = KotlinLogging.logger {}

@Component
class CSVParser {

    private val csvParser = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
        setIgnoreSurroundingSpaces(true)
    }.build()

    fun readCsv(inputStream: InputStream): List<List<String>> {
        try {
            return csvParser.parse(inputStream.reader())
                .map {
                    it.toList()
                }
        } catch (e: Exception) {
            logger.error(e) { "Error while parsing CSV" }
            throw CSVParserException("Error while parsing CSV", e)
        }
    }
}
