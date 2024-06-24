package org.jaqpot.api.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.cache.keygenerator.OrganizationsByUserKeyGenerator
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


@Configuration
class CacheConfiguration {
    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> {
        return Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
    }

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        val caffeineCacheManager = CaffeineCacheManager()
        caffeineCacheManager.setCaffeine(caffeine)
        return caffeineCacheManager
    }

    @Bean("organizationsByUserKeyGenerator")
    fun keyGenerator(): KeyGenerator {
        return OrganizationsByUserKeyGenerator()
    }
}
