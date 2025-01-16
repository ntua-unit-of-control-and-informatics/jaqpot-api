package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "docker_config")
class DockerConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "docker_config_id_seq")
    @SequenceGenerator(name = "docker_config_id_seq", sequenceName = "docker_config_id_seq", allocationSize = 1)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

    @Column(nullable = false, unique = true)
    val appName: String,

    @Column(nullable = true)
    val dockerImage: String?,

    val llmModelId: String?
) : BaseEntity()
