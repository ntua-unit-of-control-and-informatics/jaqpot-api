package org.jaqpot.api.service.runtime

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class RuntimeProvider(
    @Value("\${jaqpot.runtime.jaqpotpy-pretrained}")
    val jaqpotpyPretrainedUrl: String
)
