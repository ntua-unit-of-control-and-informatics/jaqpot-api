package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelTransformerType
import org.jaqpot.api.entity.ScoreType
import org.jaqpot.api.model.*
import java.util.*

fun Model.toDto(userDto: UserDto? = null, userCanEdit: Boolean? = null, isAdmin: Boolean? = null): ModelDto {
    return ModelDto(
        name = this.name,
        type = this.type.toDto(),
        jaqpotpyVersion = this.jaqpotpyVersion,
        libraries = this.libraries.map { it.toDto() },
        doas = this.doas.map { it.toDto(emptyMap()) }, // returning empty doa data as they're only used for inference
        dependentFeatures = this.dependentFeatures.map { it.toDto() },
        independentFeatures = this.independentFeatures.map { it.toDto() },
        visibility = this.visibility.toDto(),
        rawModel = byteArrayOf(), // returning empty byte array until https://github.com/OpenAPITools/openapi-generator/issues/17544 is fixed
        id = this.id,
        description = this.description,
        sharedWithOrganizations = this.sharedWithOrganizations.map { it.organization.toDto(organizationMembers = emptyList()) },
        task = this.task.toDto(),
        creator = userDto,
        canEdit = userCanEdit,
        isAdmin = isAdmin,
        tags = this.tags,
        selectedFeatures = this.selectedFeatures,
        legacyPredictionService = this.legacyPredictionService,
        torchConfig = this.torchConfig,
        scores = ModelScoresDto(
            train = this.trainScores?.map { it.toDto() },
            test = this.testScores?.map { it.toDto() },
            crossValidation = this.crossValidationScores?.map { it.toDto() },
        ),
        dockerConfig = this.dockerConfig?.toDto(),
        archived = this.archived,
        archivedAt = this.archivedAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}

fun ModelDto.toEntity(creatorId: String): Model {
    val m = Model(
        id = this.id,
        legacyId = null,
        creatorId = creatorId,
        name = this.name,
        description = this.description,
        type = this.type.toEntity(),
        jaqpotpyVersion = this.jaqpotpyVersion,
        libraries = mutableListOf(),
        doas = mutableListOf(),
        dependentFeatures = mutableListOf(),
        independentFeatures = mutableListOf(),
        sharedWithOrganizations = mutableListOf(),
        visibility = this.visibility.toEntity(),
        legacyPredictionService = null,
        task = this.task.toEntity(),
        tags = this.tags,
        selectedFeatures = this.selectedFeatures,
        featurizers = mutableListOf(),
        preprocessors = mutableListOf(),
        torchConfig = this.torchConfig,
        trainScores = mutableListOf(),
        testScores = mutableListOf(),
        crossValidationScores = mutableListOf(),
        rPbpkOdeSolver = this.rPbpkConfig?.odeSolver,
        rawPreprocessor = this.rawPreprocessor,
        rawModel = this.rawModel,
    )

    m.libraries.addAll(this.libraries.map { it.toEntity(m) })
    this.doas?.let { doaDtos -> m.doas.addAll(doaDtos.map { it.toEntity(m) }) }
    m.dependentFeatures.addAll(this.dependentFeatures.map { it.toEntity(m, FeatureDependency.DEPENDENT) })
    m.independentFeatures.addAll(this.independentFeatures.map { it.toEntity(m, FeatureDependency.INDEPENDENT) })
    this.featurizers?.let {
        m.featurizers.addAll(this.featurizers.map {
            it.toEntity(
                m,
                ModelTransformerType.FEATURIZER
            )
        })
    }
    this.preprocessors?.let {
        m.preprocessors.addAll(this.preprocessors.map {
            it.toEntity(
                m,
                ModelTransformerType.PREPROCESSOR
            )
        })
    }
    this.scores?.train?.let { m.trainScores = this.scores.train.map { it.toEntity(m, ScoreType.TRAIN) } }
    this.scores?.test?.let { m.testScores = this.scores.test.map { it.toEntity(m, ScoreType.TEST) } }
    this.scores?.crossValidation?.let {
        m.crossValidationScores = this.scores.crossValidation.map { it.toEntity(m, ScoreType.CROSS_VALIDATION) }
    }
    this.dockerConfig?.let { m.dockerConfig = it.toEntity(m) }

    return m
}

fun Model.toPredictionModelDto(
    rawModel: ByteArray,
    doas: List<PredictionDoaDto>,
    rawPreprocessor: ByteArray?
): PredictionModelDto {
    return PredictionModelDto(
        id = this.id!!,
        dependentFeatures = this.dependentFeatures.map { it.toDto() },
        independentFeatures = this.independentFeatures.map { it.toDto() },
        type = this.type.toDto(),
        task = this.task.toDto(),
        rawModel = this.encodeRawModel(rawModel),
        rawPreprocessor = this.encodeRawPreprocessor(rawPreprocessor),
        doas = doas,
        selectedFeatures = this.selectedFeatures ?: emptyList(),
        featurizers = this.featurizers.map { it.toDto() },
        preprocessors = this.preprocessors.map { it.toDto() },
        torchConfig = this.torchConfig,
        rPbpkOdeSolver = this.rPbpkOdeSolver,
        legacyAdditionalInfo = this.legacyAdditionalInfo,
        legacyPredictionService = this.legacyPredictionService
    )
}

private fun Model.encodeRawPreprocessor(rawPreprocessor: ByteArray?): String? {
    return if (rawPreprocessor == null) {
        null
    } else {
        Base64.getEncoder().encodeToString(rawPreprocessor)
    }
}

fun Model.encodeRawModel(rawModel: ByteArray): String {
    return if (isRModel() && isLegacyModel()) {
        // https://upci-ntua.atlassian.net/browse/JAQPOT-199
        // R models require special deserialization and base64 messes up the model
        rawModel.decodeToString()
    } else {
        Base64.getEncoder().encodeToString(rawModel)
    }
}

private fun Model.isLegacyModel(): Boolean {
    return this.legacyPredictionService != null
}

fun Model.isRModel() = this.type.name.startsWith("R_")


fun ModelDto.isRModel() = this.type.name.startsWith("R_")

fun ModelDto.isLegacyModel() = this.legacyPredictionService != null
