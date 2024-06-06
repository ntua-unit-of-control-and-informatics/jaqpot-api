package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction

@Entity
class Dataset(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_id_seq")
    @SequenceGenerator(name = "dataset_id_seq", sequenceName = "dataset_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Column(nullable = false)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DatasetType = DatasetType.PREDICTION,

    @OneToMany(mappedBy = "dataset", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("role = 'INPUT'")
    val input: MutableList<DataEntry>,

    @Enumerated(EnumType.STRING)
    var status: DatasetStatus = DatasetStatus.CREATED,

    @OneToMany(mappedBy = "dataset", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("role = 'RESULTS'")
    var results: MutableList<DataEntry>,

    var failureReason: String?
) : BaseEntity()
