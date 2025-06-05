package org.jaqpot.api.storage.s3

import org.jaqpot.api.aws.AWSConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
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
    @Value("\${aws.s3.preprocessors-bucket}")
    val preprocessorsBucketName: String,
    @Value("\${aws.s3.images-bucket}")
    val imagesBucketName: String,
    @Value("\${aws.cloudfront.images-distribution-url}")
    val cloudfrontImagesDistributionUrl: String,
    @Value("\${aws.s3.skip-web-identity:false}")
    val skipWebIdentity: Boolean
) {
    @Bean("s3Client")
    fun s3Client(awsConfig: AWSConfig): S3Client {
        val credentialsProvider = if (!skipWebIdentity) {
            WebIdentityTokenFileCredentialsProvider.create()
        } else {
            DefaultCredentialsProvider.create()
        }

        return S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.of(awsConfig.region))
            .build()
    }
}


