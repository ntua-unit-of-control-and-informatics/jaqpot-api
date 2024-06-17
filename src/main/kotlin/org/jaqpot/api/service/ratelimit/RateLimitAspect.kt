package org.jaqpot.api.service.ratelimit;

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class RateLimitAspect(
    private val userRateLimitService: UserRateLimitService,
    private val authenticationFacade: AuthenticationFacade
) {

    @Before("@annotation(WithRateLimitProtectionByUser)")
    fun before(joinPoint: JoinPoint) {
        val method = (joinPoint.signature as MethodSignature).method
        val className = method.declaringClass.simpleName
        val annotation = method.getAnnotation(WithRateLimitProtectionByUser::class.java)
        val limit = annotation.limit
        val intervalInSeconds = annotation.intervalInSeconds

        if (userRateLimitService.shouldLimitUserAccess("$className.$method.name", limit, intervalInSeconds)) {
            logger.warn { "Rate limit exceeded for method $method by user ${authenticationFacade.userId}" }
            throw RateLimitException("Rate limit exceeded for method $method by user ${authenticationFacade.userId}")
        }
    }
}
