package org.jaqpot.api.service.prediction.runtime.runtimes

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


abstract class RuntimeBase {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .responseTimeout(Duration.ofSeconds(60))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(60, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(60, TimeUnit.SECONDS))
            }
    }

    abstract fun getRuntimePath(predictionModelDto: PredictionModelDto): String

    abstract fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): Any

    abstract fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String

}
