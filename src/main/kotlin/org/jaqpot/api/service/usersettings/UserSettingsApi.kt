package org.jaqpot.api.service.usersettings

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.jaqpot.api.model.UploadUserAvatar200ResponseDto
import org.jaqpot.api.model.UploadUserAvatar400ResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Temporary workaround until https://github.com/OpenAPITools/openapi-generator/issues/8333 is fixed
 */
@RestController
@Validated
class UserSettingsAvatarApi(private val userSettingsService: UserSettingsService) {

    @Operation(
        tags = ["user-settings"],
        summary = "Upload user avatar",
        operationId = "uploadUserAvatar",
        description = """""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Avatar uploaded successfully",
                content = [Content(schema = Schema(implementation = UploadUserAvatar200ResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = [Content(schema = Schema(implementation = UploadUserAvatar400ResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "413", description = "File exceeds maximum size of 1MB")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/v1/user/avatar"],
        produces = ["application/json"],
        consumes = ["image/jpeg", "image/png", "image/webp"]
    )
    fun uploadUserAvatar(
        @Parameter(
            description = "",
            required = true
        ) @Valid @Size(max = 1048576) @RequestBody body: MultipartFile
    ): ResponseEntity<UploadUserAvatar200ResponseDto> {
        return userSettingsService.uploadUserAvatar(body)
    }
}
