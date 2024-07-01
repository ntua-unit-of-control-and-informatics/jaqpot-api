package org.jaqpot.api.storage.config

import org.springframework.beans.factory.annotation.Value
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

    @Value("\${aws.s3.endpoint}")
    private val serviceEndpoint: String? = null

    @Value("\${aws.credentials.access-key}")
    private val awsAccessKey: String? = null

    @Value("\${aws.credentials.secret-key}")
    private val awsSecretKey: String? = null

    @Value("\${aws.region}")
    private val awsRegion: String? = null

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .overrideConfiguration(ClientOverrideConfiguration.builder().build())
            .credentialsProvider(credentialsProvider)
            .endpointOverride(URI.create(serviceEndpoint))
            .region(Region.of(awsRegion))
            .forcePathStyle(true)
            .build()
    }

    private val credentialsProvider: StaticCredentialsProvider
        get() = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
        )
}
