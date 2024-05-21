package org.jaqpot.api.entity

import jakarta.persistence.*

@Embeddable
data class `Meta.kt`(
    @ElementCollection
    @CollectionTable(name = "meta_descriptions", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "description")
    val descriptions: List<String>,

    @ElementCollection
    @CollectionTable(name = "meta_titles", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "title")
    val titles: List<String>,

    @ElementCollection
    @CollectionTable(name = "meta_creators", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "creator")
    val creators: List<String>,

    val locked: Boolean
)
