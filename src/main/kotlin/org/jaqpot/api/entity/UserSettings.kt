package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class UserSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_settings_id_seq")
    @SequenceGenerator(name = "user_settings_id_seq", sequenceName = "user_settings_id_seq", allocationSize = 1)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: String,

    var darkMode: Boolean? = false,

    var collapseSidebar: Boolean? = false,
) : BaseEntity()
