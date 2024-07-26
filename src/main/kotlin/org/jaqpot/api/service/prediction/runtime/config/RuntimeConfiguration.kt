package org.jaqpot.api.service.prediction.runtime.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RuntimeConfiguration(
    @Value("\${jaqpot.runtime.jaqpotpy-inference-v6}")
    val jaqpotpyInferenceV6Url: String,
    @Value("\${jaqpot.runtime.legacy.jaqpot-inference}")
    val legacyJaqpotInference: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-0.20}")
    val legacyPythonGeneric20: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-0.22}")
    val legacyPythonGeneric22: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-0.23}")
    val legacyPythonGeneric23: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-0.24}")
    val legacyPythonGeneric24: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-1}")
    val legacyPythonGeneric1: String,

    @Value("\${jaqpot.runtime.jaqpotr}")
    val jaqpotRUrl: String
)
