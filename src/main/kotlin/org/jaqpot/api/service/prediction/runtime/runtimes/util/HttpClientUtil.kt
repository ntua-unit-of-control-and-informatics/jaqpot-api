package org.jaqpot.api.service.prediction.runtime.runtimes.util

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

class HttpClientUtil {
    companion object {

        fun generateHttpClient(
            maxIdleTimeInMin: Long,
            maxLifeTimeInMin: Long,
            responseTimeoutInMin: Long,
            readTimeoutInMin: Long,
            writeTimeoutInMin: Long
        ): HttpClient {
            val connectionProvider =
                ConnectionProvider.builder("custom")
                    .maxIdleTime(Duration.ofMinutes(maxIdleTimeInMin))
                    .maxLifeTime(Duration.ofMinutes(maxLifeTimeInMin))
                    .build()

            return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .responseTimeout(Duration.ofMinutes(responseTimeoutInMin))
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(readTimeoutInMin, TimeUnit.MINUTES))
                        .addHandlerLast(WriteTimeoutHandler(writeTimeoutInMin, TimeUnit.MINUTES))
                }
        }
    }
}
