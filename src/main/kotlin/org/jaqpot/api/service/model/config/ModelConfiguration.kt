package org.jaqpot.api.service.model.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ModelConfiguration(
    @Value("\${jaqpot.model.max-input-prediction-rows}")
    val maxInputPredictionRows: String
)
