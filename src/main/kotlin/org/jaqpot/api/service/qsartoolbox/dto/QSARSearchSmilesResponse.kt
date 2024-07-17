package org.jaqpot.api.service.qsartoolbox.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class QSARSearchSmilesResponse(
    @JsonProperty("SubstanceType") var SubstanceType: String? = null,
    @JsonProperty("ChemId") var ChemId: String? = null,
    @JsonProperty("Cas") var Cas: Int? = null,
    @JsonProperty("ECNumber") var ECNumber: String? = null,
    @JsonProperty("Smiles") var Smiles: String? = null,
    @JsonProperty("Names") var Names: ArrayList<String> = arrayListOf(),
    @JsonProperty("CasSmilesRelation") var CasSmilesRelation: String? = null
)
