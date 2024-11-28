package org.jaqpot.api.service.usersettings

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param message
 */
data class UploadUserAvatar400ResponseDto(

    @Schema(example = "Invalid file type. Allowed types are: jpg, png, webp", description = "")
    @get:JsonProperty("message") val message: kotlin.String? = null
) {

}

