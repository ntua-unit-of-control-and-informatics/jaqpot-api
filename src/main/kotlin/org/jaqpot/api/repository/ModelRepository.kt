package org.jaqpot.api.repository

import org.jaqpot.api.entity.Model
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface ModelRepository : PagingAndSortingRepository<Model, Long>, CrudRepository<Model, Long> {
    fun findAllByCreatorId(creatorId: String, pageable: Pageable): Page<Model>

    @Query(
        """
        SELECT m FROM Model m
        JOIN m.organizations o 
        JOIN o.userIds u 
        WHERE u = :userId
        """
    )
    fun findAllSharedWithUser(userId: String, pageable: Pageable): Page<Model>
}
