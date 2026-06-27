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
import java.util.*


interface DatasetRepository : CrudRepository<Dataset, Long> {
    fun findAllByUserId(userId: String, pageable: Pageable): Page<Dataset>
    fun findAllByUserIdAndModelId(userId: String, modelId: Long, pageable: Pageable): Page<Dataset>

    fun findAllByCreatedAtBefore(date: OffsetDateTime): List<Dataset>

    fun findByIdAndModelId(id: Long, modelId: Long): Optional<Dataset>

    /**
     * Datasets whose input is still stored in the database (i.e. never offloaded to object storage)
     * and that are older than [cutoff]. The model association is fetched eagerly so the results can
     * be offloaded outside of a transaction. Used by the reconciliation job to recover datasets whose
     * asynchronous offload never completed.
     */
    @Query("SELECT d FROM Dataset d JOIN FETCH d.model WHERE d.input IS NOT NULL AND d.createdAt < :cutoff")
    fun findDatasetsWithInputNotOffloaded(@Param("cutoff") cutoff: OffsetDateTime, pageable: Pageable): List<Dataset>

    @Modifying
    @Transactional
    @Query("UPDATE Dataset d SET d.input = NULL, d.result = NULL WHERE d.id = :id")
    fun setDatasetInputAndResultToNull(@Param("id") id: Long?)
}
