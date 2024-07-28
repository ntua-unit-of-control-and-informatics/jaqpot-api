package org.jaqpot.api.service.prediction.runtime.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RuntimeConfiguration(
    @Value("\${jaqpot.runtime.jaqpotpy-inference-v6}")
    val jaqpotpyInferenceV6Url: String,
    @Value("\${jaqpot.runtime.legacy.jaqpot-inference}")
    val legacyJaqpotInference: String,
    @Value("\${jaqpot.runtime.legacy.generic-python-0.20}")
    val legacyGenericPython20: String,
    @Value("\${jaqpot.runtime.legacy.generic-python-0.22}")
    val legacyGenericPython22: String,
    @Value("\${jaqpot.runtime.legacy.generic-python-0.23}")
    val legacyGenericPython23: String,
    @Value("\${jaqpot.runtime.legacy.generic-python-0.24}")
    val legacyGenericPython24: String,
    @Value("\${jaqpot.runtime.legacy.generic-python-1}")
    val legacyGenericPython1: String,

    @Value("\${jaqpot.runtime.jaqpotr}")
    val jaqpotRUrl: String
)
