package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @Column(updatable = false)
    val legacyId: String?,

    @Column(nullable = false, updatable = false)
    val creatorId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Size(min = 3, max = 255)
    @Column(nullable = false)
    var name: String,

    @Size(min = 3, max = 15000)
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

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("association_type = 'AFFILIATION'")
    val affiliatedOrganizations: MutableList<ModelOrganizationAssociation>,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: ModelVisibility,

    @Column(updatable = false)
    val legacyPredictionService: String?,

    val pretrained: Boolean?,

    @Size(min = 3, max = 300)
    @Column(columnDefinition = "TEXT")
    val tags: String?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column
    var actualModel: ByteArray?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_config", columnDefinition = "jsonb")
    val extraConfig: Map<String, Any>? = emptyMap(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "legacy_additional_info", columnDefinition = "jsonb")
    val legacyAdditionalInfo: Map<String, Any>? = emptyMap(),

    ) : BaseEntity()
//{
//    // manage bidirectional relationships
//    fun addOrganizationAssociation(organization: Organization, associationType: ModelOrganizationAssociationType) {
//        val association = ModelOrganizationAssociation(
//            model = this,
//            organization = organization,
//            associationType = associationType
//        )
//        if (associationType == ModelOrganizationAssociationType.AFFILIATION) {
//            affiliatedOrganizations.add(association)
//            organization.affiliatedModels.add(association)
//        } else {
//            sharedWithOrganizations.add(association)
//            organization.sharedModels.add(association)
//        }
//    }
//
//    fun removeOrganizationAssociation(organization: Organization) {
//        if (affiliatedOrganizations.any { it.organization == organization }) {
//            val association = affiliatedOrganizations.find { it.organization == organization }
//            if (association != null) {
//                affiliatedOrganizations.remove(association)
//                organization.affiliatedModels.remove(association)
//            }
//        } else {
//            val association = sharedWithOrganizations.find { it.organization == organization }
//            if (association != null) {
//                sharedWithOrganizations.remove(association)
//                organization.sharedModels.remove(association)
//            }
//        }
//    }
//
//    fun clearAffiliatedOrganizationAssociations() {
//        affiliatedOrganizations.forEach { association ->
//            association.organization.affiliatedModels.remove(association)
//        }
//        affiliatedOrganizations.clear()
//    }
//
//    fun clearSharedOrganizationAssociations() {
//        sharedWithOrganizations.forEach { association ->
//            association.organization.sharedModels.remove(association)
//        }
//        sharedWithOrganizations.clear()
//    }
//
//}
