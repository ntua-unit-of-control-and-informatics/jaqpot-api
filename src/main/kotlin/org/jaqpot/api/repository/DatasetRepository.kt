package org.jaqpot.api.repository

import org.jaqpot.api.entity.Dataset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository


interface DatasetRepository : CrudRepository<Dataset, Long> {
    fun findAllByUserId(userId: String, pageable: Pageable): Page<Dataset>
}
