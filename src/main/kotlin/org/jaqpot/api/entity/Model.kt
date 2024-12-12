package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    val id: Long? = null,

    @Column(updatable = false)
    val legacyId: String?,

    @Column(nullable = false, updatable = false)
    val creatorId: String,

    @Size(min = 3, max = 255)
    @Column(nullable = false)
    var name: String,

    @Size(min = 3, max = 50000)
    @Column(columnDefinition = "TEXT")
    var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ModelType,

    @Column(nullable = false)
    val jaqpotpyVersion: String,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    val libraries: MutableList<Library>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    val doas: MutableList<Doa>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    @SQLRestriction("feature_dependency = 'DEPENDENT'")
    val dependentFeatures: MutableList<Feature>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    @SQLRestriction("feature_dependency = 'INDEPENDENT'")
    val independentFeatures: MutableList<Feature>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("association_type = 'SHARE'")
    val sharedWithOrganizations: MutableList<ModelOrganizationAssociation>,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: ModelVisibility,

    @Column(updatable = false)
    val legacyPredictionService: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var task: ModelTask,

    val selectedFeatures: List<String>?,

    @Column(nullable = false)
    var archived: Boolean = false,

    var archivedAt: OffsetDateTime? = null,

    @Size(min = 3, max = 1000)
    @Column(columnDefinition = "TEXT")
    var tags: String?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column
    var rawPreprocessor: ByteArray?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column
    var rawModel: ByteArray?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "torch_config", columnDefinition = "jsonb")
    val torchConfig: Map<String, Any>? = emptyMap(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "legacy_additional_info", columnDefinition = "jsonb")
    val legacyAdditionalInfo: Map<String, Any>? = emptyMap(),

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("transformer_type = 'FEATURIZER'")
    val featurizers: MutableList<ModelTransformer>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("transformer_type = 'PREPROCESSOR'")
    val preprocessors: MutableList<ModelTransformer>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("score_type = 'TRAIN'")
    var trainScores: List<Scores>? = null,

    val rPbpkOdeSolver: String? = null,

    @OneToOne(mappedBy = "model", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    var dockerConfig: DockerConfig? = null,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("score_type = 'TEST'")
    var testScores: List<Scores>? = null,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("score_type = 'CROSS_VALIDATION'")
    var crossValidationScores: List<Scores>? = null,
) : BaseEntity() {
    fun isQsarToolboxModel() = this.type in listOf(
        ModelType.QSAR_TOOLBOX_CALCULATOR,
        ModelType.QSAR_TOOLBOX_QSAR_MODEL,
        ModelType.QSAR_TOOLBOX_PROFILER
    )
}

