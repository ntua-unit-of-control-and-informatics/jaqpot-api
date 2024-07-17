package org.jaqpot.api.service.qsartoolbox

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.service.qsartoolbox.config.QsartoolboxConfig
import org.jaqpot.api.service.qsartoolbox.dto.QSARCalculationResponse
import org.jaqpot.api.service.qsartoolbox.dto.QSARSearchSmilesResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate


@Component
class QSARToolbox(private val qsartoolboxConfig: QsartoolboxConfig) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val BIODEGRADABILITY_CALCULATOR_ID = "8a60f10c-d448-415f-80e7-2410234d2dc3"
    }

    fun searchSmiles(smiles: String): Array<QSARSearchSmilesResponse>? {
        val registerUnknown = true
        val ignoreStereo = false

        val url = "${qsartoolboxConfig.url}/api/v6/search/smiles/${registerUnknown}/${ignoreStereo}"

        val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()
        queryParams.add("smiles", smiles)

        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity(url, Array<QSARSearchSmilesResponse>::class.java, queryParams)

        return response.body
    }

    fun calculateQsarProperties(
        chemId: String,
        calculatorId: String = BIODEGRADABILITY_CALCULATOR_ID
    ): QSARCalculationResponse? {
        val url = "${qsartoolboxConfig.url}/api/v6/calculation/${BIODEGRADABILITY_CALCULATOR_ID}/${chemId}"

        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity(url, QSARCalculationResponse::class.java)

        return response.body
    }
}
