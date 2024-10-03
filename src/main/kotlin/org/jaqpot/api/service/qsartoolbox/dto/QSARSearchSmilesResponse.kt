package org.jaqpot.api.service.qsartoolbox.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class QSARSearchSmilesResponse(
    @JsonProperty("SubstanceType") val SubstanceType: String? = null,
    @JsonProperty("ChemId") val ChemId: String? = null,
    @JsonProperty("Cas") val Cas: Int? = null,
    @JsonProperty("ECNumber") val ECNumber: String? = null,
    @JsonProperty("Smiles") val Smiles: String? = null,
    @JsonProperty("Names") val Names: ArrayList<String> = arrayListOf(),
    @JsonProperty("CasSmilesRelation") val CasSmilesRelation: String? = null
)
