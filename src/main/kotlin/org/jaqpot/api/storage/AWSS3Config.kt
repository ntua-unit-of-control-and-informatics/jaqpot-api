package org.jaqpot.api.storage

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class AWSS3Config(private val env: Environment? = null) {

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .overrideConfiguration(ClientOverrideConfiguration.builder().build())
            .credentialsProvider(credentialsProvider)
            .endpointOverride(URI.create(env!!.getProperty("aws.serviceEndpoint", "")))
            .region(Region.of(env.getProperty("aws.signingRegion", "")))
            .forcePathStyle(true)
            .build()
    }

    private val credentialsProvider: StaticCredentialsProvider
        get() = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                env!!.getProperty("aws.accessKey", ""),
                env.getProperty("aws.secretKey", "")
            )
        )
}
