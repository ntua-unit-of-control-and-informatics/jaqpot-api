package org.jaqpot.api.service.model

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.jaqpot.api.model.DatasetDto
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter

@Controller
class ModelApi(private val modelService: ModelService) {
    @Operation(
        tags = ["model"],
        summary = "Stream predictions from LLM Model",
        operationId = "streamPredictWithModel",
        description = """Submit a prompt for streaming prediction using a specific LLM model""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Streaming response started",
                content = [Content(schema = Schema(implementation = kotlin.String::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid Request"),
            ApiResponse(responseCode = "404", description = "Model not found"),
            ApiResponse(responseCode = "500", description = "Internal Server Error")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/v1/models/{modelId}/predict/stream"],
        produces = ["text/event-stream"],
        consumes = ["application/json"]
    )
    fun streamPredictWithModel(
        @Parameter(
            description = "The ID of the LLM model to use for prediction",
            required = true
        ) @PathVariable("modelId") modelId: kotlin.Long,
        @Parameter(description = "", required = true) @Valid @RequestBody datasetDto: DatasetDto
    ): ResponseBodyEmitter {
        return ResponseBodyEmitter().apply {
            modelService.streamPredictWithModel(modelId, datasetDto).subscribe {
                send(it)
            }
        }
    }

}
