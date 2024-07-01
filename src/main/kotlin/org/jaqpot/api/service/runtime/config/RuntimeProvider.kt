package org.jaqpot.api.service.runtime.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RuntimeProvider(
    @Value("\${jaqpot.runtime.jaqpotpy-inference-v6}")
    val jaqpotpyInferenceV6Url: String,
    @Value("\${jaqpot.runtime.jaqpotpy-inference-legacy-legacy}")
    val jaqpotpyInferenceLegacyLegacy: String,
    @Value("\${jaqpot.runtime.jaqpotpy-inference-legacy-22}")
    val jaqpotpyInferenceLegacy22: String,
    @Value("\${jaqpot.runtime.jaqpotpy-inference-legacy-23}")
    val jaqpotpyInferenceLegacy23: String,
    @Value("\${jaqpot.runtime.jaqpotpy-inference-legacy-24}")
    val jaqpotpyInferenceLegacy24: String,
    @Value("\${jaqpot.runtime.jaqpotpy-inference-legacy-latest}")
    val jaqpotpyInferenceLegacyLatest: String,

    @Value("\${jaqpot.runtime.jaqpotr}")
    val jaqpotRUrl: String
)
