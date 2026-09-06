package org.jaqpot.api.service.qsartoolbox

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.service.qsartoolbox.config.QsartoolboxConfig
import org.jaqpot.api.service.qsartoolbox.dto.QSARSearchSmilesResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.Semaphore


@Component
class QSARToolboxAPI(
    private val qsartoolboxConfig: QsartoolboxConfig,
    private val restTemplate: RestTemplate
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Caps concurrent outbound calls to the toolbox, which runs on a single small
     * Windows host and restarts when flooded. Calls that arrive while all permits
     * are in use are rejected with 429 so the API sheds load instead of queuing
     * onto the toolbox. Initialized lazily from config because the bean is created
     * before @ConfigurationProperties binding in some test slices.
     */
    private val permits: Semaphore by lazy {
        Semaphore(qsartoolboxConfig.maxConcurrentRequests.coerceAtLeast(1))
    }

    private fun <T> withToolboxPermit(action: String, call: () -> T): T {
        if (!permits.tryAcquire()) {
            logger.warn { "Rejecting QSAR Toolbox $action: all ${qsartoolboxConfig.maxConcurrentRequests} concurrent permits in use" }
            throw ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "QSAR Toolbox is busy, please retry in a few seconds"
            )
        }
        try {
            return call()
        } finally {
            permits.release()
        }
    }

    fun searchSmiles(smiles: String): Array<QSARSearchSmilesResponse>? {
        val registerUnknown = true
        val ignoreStereo = false

        val url = "${qsartoolboxConfig.url}/api/v6/search/smiles/${registerUnknown}/${ignoreStereo}?smiles={smiles}"

        return withToolboxPermit("searchSmiles") {
            restTemplate.getForEntity(url, Array<QSARSearchSmilesResponse>::class.java, smiles).body
        }
    }

    fun runQsarModel(
        chemId: String,
        qsarGuid: String
    ): Map<*, *>? {
        val url = "${qsartoolboxConfig.url}/api/v6/qsar/apply/${qsarGuid}/${chemId}"

        return withToolboxPermit("runQsarModel") {
            restTemplate.getForEntity(url, Map::class.java).body
        }
    }

    fun runProfiler(
        chemId: String,
        profilerGuid: String
    ): List<String>? {
        val url = "${qsartoolboxConfig.url}/api/v6/profiling/${profilerGuid}/${chemId}"

        return withToolboxPermit("runProfiler") {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<String>>() {}
            ).body
        }
    }


    fun runCalculator(
        chemId: String,
        calculatorId: String
    ): Map<*, *>? {
        val url = "${qsartoolboxConfig.url}/api/v6/calculation/${calculatorId}/${chemId}"

        return withToolboxPermit("runCalculator") {
            restTemplate.getForEntity(url, Map::class.java).body
        }
    }
}
