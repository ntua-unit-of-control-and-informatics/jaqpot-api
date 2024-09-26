package org.jaqpot.api.service.ratelimit

import io.github.bucket4j.BandwidthBuilder.BandwidthBuilderCapacityStage
import io.github.bucket4j.Bucket
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

    fun shouldLimitUserAccess(methodName: String, limit: Long, intervalInSeconds: Long): Boolean {
        // do not rate limit public endpoints
        if (!authenticationFacade.isLoggedIn) return false

        logger.info { "Rate limiting user access for method $methodName" }
        if (authenticationFacade.isAdmin) {
            return false
        } else if (rateLimitedEndpointsByPlan.contains(methodName)) {
            if (authenticationFacade.isEnterpriseUser) {
                return false
            } else if (authenticationFacade.isProUser) {
                // TODO implement logic with pro user rate limit based on prediction credits
            }
        }

        val userIdBucketKey = getUserIdBucketKey(authenticationFacade.userId, methodName)
        val userBucket =
            Optional.ofNullable(userIdBuckets[userIdBucketKey]).orElseGet {
                // create map if not exists
                userIdBuckets[userIdBucketKey] = createBucket(
                    limit,
                    intervalInSeconds
                )

                userIdBuckets[userIdBucketKey]
            }


        val limitIsReached = !userBucket.tryConsume(1)

        return limitIsReached
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
