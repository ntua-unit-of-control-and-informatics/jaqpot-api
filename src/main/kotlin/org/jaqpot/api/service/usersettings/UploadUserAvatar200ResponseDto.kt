package org.jaqpot.api.service.usersettings

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param avatarUrl The CDN URL of the uploaded avatar
 */
data class UploadUserAvatar200ResponseDto(

    @Schema(
        example = "https://d2zoqz4gyxc03g.cloudfront.net/avatars/123e4567-e89b-12d3-a456-426614174000.jpg",
        description = "The CDN URL of the uploaded avatar"
    )
    @get:JsonProperty("avatarUrl") val avatarUrl: kotlin.String? = null
) {

}

