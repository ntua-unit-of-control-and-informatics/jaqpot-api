package org.jaqpot.api.repository

import jakarta.transaction.Transactional
import org.jaqpot.api.entity.Model
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import java.util.*


interface ModelRepository : PagingAndSortingRepository<Model, Long>, CrudRepository<Model, Long> {

    fun findAllByCreatorId(creatorId: String, pageable: Pageable): Page<Model>
    fun findOneByLegacyId(legacyId: String): Optional<Model>

    @Query(
        """
        SELECT m FROM Model m
        JOIN m.affiliatedOrganizations o 
        WHERE o.organization.id = :organizationId AND m.visibility = 'PUBLIC'
        """
    )
    fun findAllByAffiliatedOrganizations(organizationId: Long, pageable: Pageable): Page<Model>

    @Query(
        """
        SELECT m FROM Model m
        JOIN m.sharedWithOrganizations o 
        LEFT JOIN o.organization.userIds u 
        WHERE (u = :userId OR o.organization.creatorId = :userId)
        """
    )
    fun findAllSharedWithUser(userId: String, pageable: Pageable): Page<Model>

    @Query(
        """
        SELECT m FROM Model m
        JOIN m.sharedWithOrganizations o 
        LEFT JOIN o.organization.userIds u 
        WHERE (u = :userId OR o.organization.creatorId = :userId) AND o.organization.id = :organizationId
        """
    )
    fun findAllSharedWithUserByOrganizationId(userId: String, pageable: Pageable, organizationId: Long): Page<Model>

    @Query(
        value = """
            SELECT *, ts_rank_cd(textsearchable_index_col, to_tsquery(:query)) AS rank 
            FROM model, to_tsquery(:query) query
            WHERE model.visibility = 'PUBLIC' AND textsearchable_index_col @@ query
            ORDER BY rank DESC
            """,
//        countQuery = """
//            SELECT COUNT(*) FROM model
//            WHERE textsearchable_index_col @@ to_tsquery(:queryString)
//            """,
        nativeQuery = true
    )
    fun searchModelsBy(query: String, pageable: Pageable): Page<Model>


    @Modifying
    @Transactional
    @Query("UPDATE Model m SET m.actualModel = NULL WHERE m.id = :id")
    fun setActualModelToNull(@Param("id") id: Long?)
}
