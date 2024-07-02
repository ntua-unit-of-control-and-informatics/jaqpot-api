package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureType
import org.jaqpot.api.model.FeatureTypeDto

fun FeatureTypeDto.toEntity(): FeatureType {
    return when (this) {
        FeatureTypeDto.INTEGER -> FeatureType.INTEGER
        FeatureTypeDto.FLOAT -> FeatureType.FLOAT
        FeatureTypeDto.CATEGORICAL -> FeatureType.CATEGORICAL
        FeatureTypeDto.STRING -> FeatureType.STRING
        FeatureTypeDto.TEXT -> FeatureType.TEXT
        FeatureTypeDto.SMILES -> FeatureType.SMILES
    }
}

fun FeatureType.toDto(): FeatureTypeDto {
    return when (this) {
        FeatureType.INTEGER -> FeatureTypeDto.INTEGER
        FeatureType.FLOAT -> FeatureTypeDto.FLOAT
        FeatureType.CATEGORICAL -> FeatureTypeDto.CATEGORICAL
        FeatureType.STRING -> FeatureTypeDto.STRING
        FeatureType.TEXT -> FeatureTypeDto.TEXT
        FeatureType.SMILES -> FeatureTypeDto.SMILES
    }
}

