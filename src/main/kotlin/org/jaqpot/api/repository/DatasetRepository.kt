package org.jaqpot.api.repository

import jakarta.transaction.Transactional
import org.jaqpot.api.entity.Dataset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime


interface DatasetRepository : CrudRepository<Dataset, Long> {
    fun findAllByUserId(userId: String, pageable: Pageable): Page<Dataset>

    fun findAllByCreatedAtBefore(date: OffsetDateTime): List<Dataset>

    @Modifying
    @Transactional
    @Query("UPDATE Dataset d SET d.input = NULL, d.result = NULL WHERE d.id = :id")
    fun setDatasetInputAndResultToNull(@Param("id") id: Long?)
}
