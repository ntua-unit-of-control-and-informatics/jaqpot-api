package org.jaqpot.api.service.ratelimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithRateLimitProtectionByUser(val limit: Long, val intervalInSeconds: Long)
