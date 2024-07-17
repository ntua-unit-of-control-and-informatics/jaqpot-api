package org.jaqpot.api.service.qsartoolbox.dto

import com.google.gson.annotations.SerializedName


data class QSARSearchSmilesResponse(

    @SerializedName("SubstanceType") var SubstanceType: String? = null,
    @SerializedName("ChemId") var ChemId: String? = null,
    @SerializedName("Cas") var Cas: Int? = null,
    @SerializedName("ECNumber") var ECNumber: String? = null,
    @SerializedName("Smiles") var Smiles: String? = null,
    @SerializedName("Names") var Names: ArrayList<String> = arrayListOf(),
    @SerializedName("CasSmilesRelation") var CasSmilesRelation: String? = null

)
