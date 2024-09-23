package org.jaqpot.api.repository

import org.jaqpot.api.entity.Lead
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LeadRepository : CrudRepository<Lead, Long> {
    fun findByEmail(email: String): Optional<Lead>
}
