package org.jaqpot.api.service.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Flux
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class RateLimitAspect(
    private val userRateLimitService: UserRateLimitService
) {

    @Around("@annotation(WithRateLimitProtectionByUser)")
    fun around(joinPoint: ProceedingJoinPoint): Any {
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

        return when {
            // If rate limit check failed, proceed without modification
            consumptionProbeOptional.isEmpty -> joinPoint.proceed()

            // Handle different return types
            method.returnType == Flux::class.java -> handleFluxReturn(joinPoint, consumptionProbeOptional.get(), limit)
            else -> handleResponseEntityReturn(joinPoint, consumptionProbeOptional.get(), limit)
        }
    }

    private fun handleFluxReturn(
        joinPoint: ProceedingJoinPoint,
        consumptionProbe: ConsumptionProbe,
        limit: Long
    ): Flux<*> {
        val result = joinPoint.proceed() as Flux<*>

        return result.transform { flux ->
            flux.contextWrite { context ->
                context.put("X-Rate-Limit-Limit", limit.toString())
                    .put("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
                    .put(
                        "X-Rate-Limit-Reset",
                        OffsetDateTime.now().plusNanos(consumptionProbe.nanosToWaitForReset).toString()
                    )
            }
        }
    }

    private fun handleResponseEntityReturn(
        joinPoint: ProceedingJoinPoint,
        consumptionProbe: ConsumptionProbe,
        limit: Long
    ): ResponseEntity<*> {
        val responseEntity = joinPoint.proceed() as ResponseEntity<*>

        val newHeaders = LinkedMultiValueMap<String, String>()
        newHeaders.addAll(responseEntity.headers)
        newHeaders.add("X-Rate-Limit-Limit", limit.toString())
        newHeaders.add("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
        newHeaders.add(
            "X-Rate-Limit-Reset",
            OffsetDateTime.now().plusNanos(consumptionProbe.nanosToWaitForReset).toString()
        )

        return ResponseEntity(responseEntity.body, newHeaders, responseEntity.statusCode)
    }
}
