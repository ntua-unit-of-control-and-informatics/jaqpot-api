package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class DataEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_entry_id_seq")
    @SequenceGenerator(name = "data_entry_id_seq", sequenceName = "data_entry_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne
    @JoinColumn(name = "dataset_id", updatable = false, nullable = false)
    val dataset: Dataset,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DataEntryType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "values", columnDefinition = "jsonb", nullable = false)
    val values: Any,
) : BaseEntity()
