package org.jaqpot.api.service.runtime

import org.jaqpot.api.entity.Model
import org.jaqpot.api.service.runtime.config.RuntimeProvider
import org.springframework.stereotype.Component

@Component
class RuntimeResolver(val runtimeProvider: RuntimeProvider) {

    fun resolveRuntime(model: Model): String {
        return runtimeProvider.jaqpotpyPretrainedUrl
    }
}
