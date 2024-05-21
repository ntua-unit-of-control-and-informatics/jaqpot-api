package org.jaqpot.api.entity.legacy

//class LegacyModel(
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    val id: Long = 0,
//
//    @Column(name = "legacy_model_id", unique = true, nullable = true)
//    val legacyModelId: String,
//
//    @Deprecated("Property visible is deprecated. Use public instead")
//    val visible: Boolean,
//
//    @Deprecated("Part of the old jaqpot model. Use libraries instead.")
//    @ElementCollection
//    @CollectionTable(name = "legacy_model_libraries", joinColumns = [JoinColumn(name = "legacy_model_id")])
//    @Column(name = "library")
//    val librariesLegacy: List<String>,
//
//    @Deprecated("Part of the old jaqpot model. Use libraries instead.")
//    @ElementCollection
//    @CollectionTable(name = "legacy_model_library_versions", joinColumns = [JoinColumn(name = "legacy_model_id")])
//    @Column(name = "library_version")
//    val legacyLibraryVersions: List<String>,
//
//    @Deprecated("Part of the old jaqpot model. Use features instead.")
//    @ElementCollection
//    @CollectionTable(name = "legacy_model_independent_features", joinColumns = [JoinColumn(name = "legacy_model_id")])
//    @Column(name = "independent_feature")
//    val legacyIndependentFeatures: List<String>,
//
//    @Deprecated("Part of the old jaqpot model.")
//    @ElementCollection
//    @CollectionTable(name = "legacy_model_additional_info", joinColumns = [JoinColumn(name = "legacy_model_id")])
//    @MapKeyColumn(name = "feature_uri")
//    @Column(name = "additional_info")
//    val legacyAdditionalInfo: Map<String, String>,
//)
