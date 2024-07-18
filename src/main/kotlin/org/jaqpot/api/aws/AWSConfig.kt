package org.jaqpot.api.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class AWSConfig(
    @Value("\${aws.region}")
    val region: String,
)
