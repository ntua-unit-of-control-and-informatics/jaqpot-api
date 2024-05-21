package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @Column(name = "model_id", unique = true, nullable = false)
    val modelId: String,

    @Embedded
    val meta: `Meta.kt`,

    val visible: Boolean,
    val public: Boolean,
    val type: String,

    val jaqpotpyVersion: String,

    @ElementCollection
    @CollectionTable(name = "model_libraries", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "library")
    val libraries: List<String>,

    @ElementCollection
    @CollectionTable(name = "model_library_versions", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "library_version")
    val libraryVersions: List<String>,

    @ElementCollection
    @CollectionTable(name = "model_dependent_features", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "dependent_feature")
    val dependentFeatures: List<String>,

    @ElementCollection
    @CollectionTable(name = "model_independent_features", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "independent_feature")
    val independentFeatures: List<String>,

    @ElementCollection
    @CollectionTable(name = "model_predicted_features", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "predicted_feature")
    val predictedFeatures: List<String>,

    val reliability: Int,

    @Embedded
    val runtime: Runtime,

    @Lob
    val actualModel: ByteArray,

    @ElementCollection
    @CollectionTable(name = "model_additional_info", joinColumns = [JoinColumn(name = "model_id")])
    @MapKeyColumn(name = "feature_uri")
    @Column(name = "additional_info")
    val additionalInfo: Map<String, String>,

    val pretrained: Boolean
) : BaseEntity()
