package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class Dataset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "dataset_id", unique = true, nullable = false)
    val datasetId: String,

    @Embedded
    val meta: `Meta.kt`,

    val visible: Boolean,
    val featured: Boolean,

    @OneToMany(mappedBy = "dataset", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val dataEntries: List<DataEntry>,

    @ElementCollection
    @CollectionTable(name = "dataset_features", joinColumns = [JoinColumn(name = "dataset_id")])
    @Column(name = "feature")
    val features: List<FeatureRef>,

    val totalRows: Int,
    val totalColumns: Int,
    val existence: String,
    val doa: Boolean
)

@Embeddable
class FeatureRef(
    val name: String,
    val key: String,
    val uri: String
)
