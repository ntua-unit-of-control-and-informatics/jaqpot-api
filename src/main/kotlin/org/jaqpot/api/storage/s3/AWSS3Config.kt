package org.jaqpot.api.storage.s3

import org.jaqpot.api.aws.AWSConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class AWSS3Config(
    @Value("\${aws.s3.models-bucket}")
    val modelsBucketName: String,
    @Value("\${aws.s3.datasets-bucket}")
    val datasetsBucketName: String,
    @Value("\${aws.s3.doas-bucket}")
    val doasBucketName: String,
) {
    @Bean("s3Client")
    fun s3Client(awsConfig: AWSConfig): S3Client {
        return S3Client.builder()
            .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
            .region(Region.of(awsConfig.region))
            .build()
    }
}


