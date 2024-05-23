package org.jaqpot.api.service.runtime

import org.springframework.stereotype.Component

@Component
class RuntimeResolver(val runtimeProvider: RuntimeProvider) {

    fun resolveRuntime(): String {
        return runtimeProvider.jaqpotpyPretrainedUrl;
    }
}
