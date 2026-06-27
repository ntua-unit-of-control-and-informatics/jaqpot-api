package org.jaqpot.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * Configures the executor backing every `@Async` method (most importantly the dataset/prediction
 * offloading in [org.jaqpot.api.service.prediction.rest.RESTPredictionService]).
 *
 * Spring Boot's default async executor uses an **unbounded** task queue. When the worker threads
 * stall (e.g. a downstream call hangs), offload tasks pile up in that queue silently and forever:
 * the dataset row keeps its input in Postgres, nothing is written to S3, and no error is ever
 * logged. To make such a situation self-correcting and observable we:
 *
 *  - bound the queue and apply [ThreadPoolExecutor.CallerRunsPolicy] so that, once saturated, the
 *    submitting thread runs the task itself (backpressure) instead of the work being lost;
 *  - drain in-flight tasks on shutdown so offloads are not dropped during a deploy/restart;
 *  - log any exception thrown from a void/`Unit`-returning `@Async` method, which Spring would
 *    otherwise swallow.
 *
 * The bean is intentionally named `applicationTaskExecutor` so that Spring Boot's auto-configured
 * executor backs off and Micrometer auto-instruments it (queue size, active threads, etc.).
 */
@Configuration
class AsyncConfig : AsyncConfigurer {

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val CORE_POOL_SIZE = 8
        private const val MAX_POOL_SIZE = 16
        private const val QUEUE_CAPACITY = 100
        private const val AWAIT_TERMINATION_SECONDS = 60
    }

    @Bean(name = ["applicationTaskExecutor", "taskExecutor"])
    fun applicationTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = CORE_POOL_SIZE
        executor.maxPoolSize = MAX_POOL_SIZE
        executor.queueCapacity = QUEUE_CAPACITY
        executor.setThreadNamePrefix("jaqpot-async-")
        // Backpressure instead of silent task loss when the pool and queue are full.
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        // Drain queued/in-flight offloads on shutdown rather than dropping them.
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS)
        return executor
    }

    override fun getAsyncExecutor(): Executor {
        return applicationTaskExecutor()
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { throwable, method, params ->
            logger.error(throwable) {
                "Uncaught exception in @Async method ${method.declaringClass.simpleName}.${method.name} " +
                    "with ${params.size} argument(s)"
            }
        }
    }
}
