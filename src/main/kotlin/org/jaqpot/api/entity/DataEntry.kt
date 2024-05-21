package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class DataEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    val dataset: Dataset,

    @Embedded
    val entryId: EntryId,

    @ElementCollection
    @CollectionTable(name = "data_entry_values", joinColumns = [JoinColumn(name = "data_entry_id")])
    @MapKeyColumn(name = "value_key")
    @Column(name = "value")
    val values: Map<String, Int>
)

@Embeddable
class EntryId(
    val name: String,
    val ownerUUID: String?,
    val type: String?,
    val uri: String?
)
