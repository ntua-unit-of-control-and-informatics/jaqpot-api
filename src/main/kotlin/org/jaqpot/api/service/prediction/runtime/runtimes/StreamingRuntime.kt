package org.jaqpot.api.service.prediction.runtime.runtimes

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

abstract class StreamingRuntime : RuntimeBase() {

    companion object {
        private val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .responseTimeout(Duration.ofSeconds(60))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(60, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(60, TimeUnit.SECONDS))
            }
    }

    abstract fun sendStreamingPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Flux<String>

    open fun getHttpClient(): HttpClient {
        return httpClient
    }
}
