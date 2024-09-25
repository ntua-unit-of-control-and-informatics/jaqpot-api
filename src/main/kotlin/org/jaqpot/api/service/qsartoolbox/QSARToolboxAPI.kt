package org.jaqpot.api.service.qsartoolbox

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.service.qsartoolbox.config.QsartoolboxConfig
import org.jaqpot.api.service.qsartoolbox.dto.QSARSearchSmilesResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class QSARToolboxAPI(private val qsartoolboxConfig: QsartoolboxConfig) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun searchSmiles(smiles: String): Array<QSARSearchSmilesResponse>? {
        val registerUnknown = true
        val ignoreStereo = false

        val url = "${qsartoolboxConfig.url}/api/v6/search/smiles/${registerUnknown}/${ignoreStereo}?smiles={smiles}"

        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity(url, Array<QSARSearchSmilesResponse>::class.java, smiles)

        return response.body
    }

    fun calculateQsarProperties(
        chemId: String,
        calculatorId: String
    ): Map<*, *>? {
        val url = "${qsartoolboxConfig.url}/api/v6/calculation/${calculatorId}/${chemId}"

        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity(url, Map::class.java)

        return response.body
    }
}
