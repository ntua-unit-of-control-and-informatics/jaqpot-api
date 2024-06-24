package org.jaqpot.api.cache.keygenerator

import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method

class OrganizationsByUserKeyGenerator : KeyGenerator {
    override fun generate(target: Any, method: Method, vararg params: Any?): Any {
        return params[1]!!
    }
}
