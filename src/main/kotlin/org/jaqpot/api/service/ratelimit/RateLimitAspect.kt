package org.jaqpot.api.service.ratelimit

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class RateLimitAspect(
    private val userRateLimitService: UserRateLimitService
) {

    @Around("@annotation(WithRateLimitProtectionByUser)")
    fun around(joinPoint: JoinPoint): ResponseEntity<*> {
        val responseEntity = (joinPoint as ProceedingJoinPoint).proceed() as ResponseEntity<*>
        val method = (joinPoint.signature as MethodSignature).method
        val className = method.declaringClass.simpleName
        val annotation = method.getAnnotation(WithRateLimitProtectionByUser::class.java)
        val limit = annotation.limit
        val intervalInSeconds = annotation.intervalInSeconds

        val consumptionProbeOptional = userRateLimitService.incrementMethodUsage(
            "$className.${method.name}",
            limit,
            intervalInSeconds
        )

        if (consumptionProbeOptional.isEmpty) {
            return responseEntity
        }
        val consumptionProbe = consumptionProbeOptional.get()

        val newHeaders = LinkedMultiValueMap<String, String>()
        newHeaders.addAll(responseEntity.headers)
        newHeaders.add("X-Rate-Limit-Limit", limit.toString())
        newHeaders.add("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
        newHeaders.add(
            "X-Rate-Limit-Reset",
            OffsetDateTime.now().plusNanos(consumptionProbe.nanosToWaitForReset).toString()
        )

        // Return a new ResponseEntity with updated headers
        return ResponseEntity(responseEntity.body, newHeaders, responseEntity.statusCode)
    }
}
