package org.jaqpot.api.service.model

import org.jaqpot.api.FeatureApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.FeatureDto
import org.jaqpot.api.model.FeatureTypeDto
import org.jaqpot.api.model.PartiallyUpdateModelFeatureRequestDto
import org.jaqpot.api.repository.FeatureRepository
import org.jaqpot.api.repository.ModelRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FeatureService(private val modelRepository: ModelRepository, private val featureRepository: FeatureRepository) :
    FeatureApiDelegate {
    @PreAuthorize("@partialFeatureUpdateAuthorizationLogic.decide(#root, #modelId)")
    override fun partiallyUpdateModelFeature(
        modelId: Long,
        featureId: Long,
        partiallyUpdateModelFeatureRequestDto: PartiallyUpdateModelFeatureRequestDto
    ): ResponseEntity<FeatureDto> {
        modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val feature = featureRepository.findById(featureId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Feature with id $featureId not found")
        }

        partiallyUpdateModelFeatureRequestDto.name.let { feature.name = it }
        partiallyUpdateModelFeatureRequestDto.units.let { feature.units = it }
        partiallyUpdateModelFeatureRequestDto.range.let { feature.range = it }
        partiallyUpdateModelFeatureRequestDto.description?.let { feature.description = it }
        partiallyUpdateModelFeatureRequestDto.featureType.let { feature.featureType = it.toEntity() }
        if (partiallyUpdateModelFeatureRequestDto.featureType != FeatureTypeDto.CATEGORICAL) {
            feature.possibleValues = null
        } else {
            partiallyUpdateModelFeatureRequestDto.possibleValues?.let { possibleValues ->
                feature.possibleValues = possibleValues.map { it.toEntity() }
            }
        }

        val featureDto = featureRepository.save(feature).toDto()

        return ResponseEntity.ok(featureDto)
    }
}
