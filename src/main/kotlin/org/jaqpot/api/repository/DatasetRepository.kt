package org.jaqpot.api.repository

import jakarta.transaction.Transactional
import org.jaqpot.api.entity.Dataset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository


interface DatasetRepository : CrudRepository<Dataset, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Dataset d SET d.status = 'EXECUTING', d.executedAt = current_timestamp WHERE d.id = :id")
    fun updateStatusToExecuting(id: Long)
    fun findAllByUserId(userId: String, pageable: Pageable): Page<Dataset>
}
