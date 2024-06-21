package org.jaqpot.api.repository

import org.jaqpot.api.entity.Model
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface ModelRepository : PagingAndSortingRepository<Model, Long>, CrudRepository<Model, Long> {
    fun findAllByCreatorId(creatorId: String, pageable: Pageable): Page<Model>
    fun findOneByLegacyId(legacyId: String): Optional<Model>

    @Query(
        """
        SELECT m FROM Model m
        JOIN m.organizations o 
        JOIN o.userIds u 
        WHERE u = :userId
        """
    )
    fun findAllSharedWithUser(userId: String, pageable: Pageable): Page<Model>

    @Query(
        value = """
            SELECT *, ts_rank_cd(textsearchable_index_col, query) AS rank 
            FROM model, to_tsquery('Weighted | (dark & matter)') query
            WHERE textsearchable_index_col @@ query
            ORDER BY rank DESC
            """,
//        countQuery = """
//            SELECT COUNT(*) FROM model
//            WHERE textsearchable_index_col @@ to_tsquery(:queryString)
//            """,
        nativeQuery = true
    )
    fun searchModelsBy(query: String, pageable: Pageable): Page<Model>
}
