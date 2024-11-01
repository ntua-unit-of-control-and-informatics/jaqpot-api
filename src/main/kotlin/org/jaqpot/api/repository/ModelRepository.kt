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
        WHERE m.visibility = 'ORG_SHARED'
          AND EXISTS (
            SELECT 1 FROM m.sharedWithOrganizations o
            JOIN o.organization org
            LEFT JOIN org.organizationMembers u
            WHERE u.userId = :userId OR org.creatorId = :userId
         )
        """
    )
    fun findAllSharedWithUser(userId: String, pageable: Pageable): Page<Model>

    @Query(
        """
        SELECT m FROM Model m
        WHERE m.visibility = 'ORG_SHARED'
          AND EXISTS (
            SELECT 1 FROM m.sharedWithOrganizations o
            JOIN o.organization org
            LEFT JOIN org.organizationMembers u
            WHERE (u.userId = :userId OR org.creatorId = :userId) AND o.organization.id = :organizationId
         )
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

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Model m SET m.rawModel = NULL WHERE m.id = :id")
    fun setRawModelToNull(@Param("id") id: Long?)


    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Model m SET m.rawPreprocessor = NULL WHERE m.id = :id")
    fun setRawPreprocessorToNull(@Param("id") id: Long?)
}
