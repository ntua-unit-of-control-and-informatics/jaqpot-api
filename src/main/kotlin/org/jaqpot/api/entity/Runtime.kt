package org.jaqpot.api.entity

import jakarta.persistence.*
import lombok.RequiredArgsConstructor

@Embeddable
@RequiredArgsConstructor
class Runtime(
    @Embedded
    val meta: `Meta.kt`,

    @ElementCollection
    @CollectionTable(name = "algorithm_ontological_classes", joinColumns = [JoinColumn(name = "model_id")])
    @Column(name = "ontological_class")
    val ontologicalClasses: List<String>,

    val ranking: Int,
    val predictionService: String,
    val algorithmId: String
)
