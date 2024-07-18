package org.jaqpot.api.storage.s3

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.aws.AWSConfig
import org.jaqpot.api.storage.Storage
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*


@Service
class S3Storage(
    private val awsConfig: AWSConfig, private val s3Client: S3Client
) : Storage {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getObject(bucketName: String, keyName: String): Optional<ByteArray> {
        logger.info { "Downloading object from S3 bucket $bucketName with key $keyName on region ${awsConfig.region}" }
        val request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(keyName)
            .build()

        try {
            val responseInputStream = s3Client.getObjectAsBytes(request)
            return Optional.of(responseInputStream.asByteArray())
        } catch (e: NoSuchKeyException) {
            return Optional.empty()
        }
    }

    override fun putObject(bucketName: String, keyName: String, obj: ByteArray, metadata: Map<String, String>) {
        logger.info { "Uploading object to S3 bucket $bucketName with key $keyName on region ${awsConfig.region}" }

        val request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(keyName)
            .metadata(metadata)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(obj))
    }

}
