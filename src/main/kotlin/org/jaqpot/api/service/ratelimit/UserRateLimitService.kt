package org.jaqpot.api.service.ratelimit

import io.github.bucket4j.BandwidthBuilder.BandwidthBuilderCapacityStage
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Service
class UserRateLimitService(private val authenticationFacade: AuthenticationFacade) {

    private val userIdBuckets: ConcurrentHashMap<String, Bucket> = ConcurrentHashMap()

    private fun getUserIdBucketKey(userId: String, methodName: String): String {
        return "$userId-$methodName"
    }

    private val rateLimitedEndpointsByPlan = listOf(
        "ModelService.predictWithModel",
        "ModelService.predictWithModelCSV",
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private fun isUserRateLimited(methodName: String): Boolean {
        // do not rate limit public endpoints
        if (!authenticationFacade.isLoggedIn) return false

        if (authenticationFacade.isAdmin) {
            return false
        } else if (rateLimitedEndpointsByPlan.contains(methodName)) {
            if (authenticationFacade.isEnterpriseUser) {
                return false
            } else if (authenticationFacade.isProUser) {
                // TODO implement logic with pro user rate limit based on prediction credits
            }
        }
        return true
    }

    fun incrementMethodUsage(methodName: String, limit: Long, intervalInSeconds: Long): Optional<ConsumptionProbe> {
        if (!isUserRateLimited(methodName)) {
            return Optional.empty()
        }

        val userBucket =
            retrieveRateLimitingBucket(methodName, limit, intervalInSeconds)

        val consumptionProbe = userBucket.tryConsumeAndReturnRemaining(1)

        if (consumptionProbe.remainingTokens <= 0) {
            throw RateLimitException("Rate limit exceeded for method $methodName by user ${authenticationFacade.userId}")
        }

        return Optional.of(consumptionProbe)
    }

    private fun retrieveRateLimitingBucket(
        methodName: String,
        limit: Long,
        intervalInSeconds: Long
    ): Bucket {
        val userIdBucketKey = getUserIdBucketKey(authenticationFacade.userId, methodName)
        return if (userIdBuckets.containsKey(userIdBucketKey)) {
            userIdBuckets[userIdBucketKey]!!
        } else {
            userIdBuckets[userIdBucketKey] = createBucket(
                limit,
                intervalInSeconds
            )
            return userIdBuckets[userIdBucketKey]!!
        }
    }

    private fun createBucket(
        limitAmount: Long,
        intervalInSeconds: Long
    ): Bucket {
        return Bucket.builder()
            .addLimit { limit: BandwidthBuilderCapacityStage ->
                limit.capacity(
                    limitAmount
                ).refillGreedy(limitAmount, Duration.ofSeconds(intervalInSeconds))
            }
            .build()
    }
}
