package org.jaqpot.api.repository

import jakarta.transaction.Transactional
import org.jaqpot.api.entity.Doa
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface DoaRepository : CrudRepository<Doa, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Doa d SET d.rawDoa = NULL WHERE d.id = :id")
    fun setRawDoaToNull(@Param("id") id: Long?)
}
