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
        return Caffeine.newBuilder().maximumWeight(1_000).expireAfterWrite(5, TimeUnit.MINUTES)
    }

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        val manager = CaffeineCacheManager()

        manager.registerCustomCache(
            CacheKeys.ALL_PUBLIC_ORGANIZATIONS,
            Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build()
        )

        manager.registerCustomCache(
            CacheKeys.USER_ORGANIZATIONS,
            Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build()
        )

        manager.registerCustomCache(
            CacheKeys.SEARCH_MODELS,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build()
        )

        manager.registerCustomCache(
            CacheKeys.ALL_API_KEYS,
            Caffeine.newBuilder()
                // TODO if we ever have more than 100_000 keys, we should consider a different approach xD
                .maximumSize(100_000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build()
        )

        return manager
    }

    @Bean("organizationsByUserKeyGenerator")
    fun keyGenerator(): KeyGenerator {
        return OrganizationsByUserKeyGenerator()
    }
}
