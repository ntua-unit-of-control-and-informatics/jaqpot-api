package org.jaqpot.api.service.qsartoolbox.dto

import com.google.gson.annotations.SerializedName


data class QSARCalculationResponse(

    @SerializedName("Value") var Value: String? = null,
    @SerializedName("Qualifier") var Qualifier: String? = null,
    @SerializedName("MinValue") var MinValue: String? = null,
    @SerializedName("MinQualifier") var MinQualifier: String? = null,
    @SerializedName("MaxValue") var MaxValue: String? = null,
    @SerializedName("MaxQualifier") var MaxQualifier: String? = null,
    @SerializedName("Unit") var Unit: String? = null,
    @SerializedName("Family") var Family: String? = null

)
