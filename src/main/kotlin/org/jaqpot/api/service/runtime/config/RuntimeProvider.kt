package org.jaqpot.api.service.runtime.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RuntimeProvider(
    @Value("\${jaqpot.runtime.jaqpotpy-inference-v6}")
    val jaqpotpyInferenceV6Url: String,
    @Value("\${jaqpot.runtime.legacy.jaqpot-inference}")
    val legacyJaqpotInference: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-latest}")
    val legacyPythonGenericLatest: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-22}")
    val legacyPythonGeneric22: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-23}")
    val legacyPythonGeneric23: String,
    @Value("\${jaqpot.runtime.legacy.python-generic-24}")
    val legacyPythonGeneric24: String,

    @Value("\${jaqpot.runtime.jaqpotr}")
    val jaqpotRUrl: String
)
