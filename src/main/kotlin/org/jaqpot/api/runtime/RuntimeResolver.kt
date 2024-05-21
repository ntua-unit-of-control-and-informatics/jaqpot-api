package org.jaqpot.api.runtime

import org.springframework.stereotype.Component

@Component
class RuntimeResolver(val runtimeProvider: RuntimeProvider) {

    fun resolveRuntime(): String {
        return runtimeProvider.jaqpotpyPretrainedUrl;
    }
}
