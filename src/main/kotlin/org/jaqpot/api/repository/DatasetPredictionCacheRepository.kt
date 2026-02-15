package org.jaqpot.api.repository

import org.jaqpot.api.entity.DatasetPredictionCache
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DatasetPredictionCacheRepository : JpaRepository<DatasetPredictionCache, Long> {
    fun findByModelIdAndInputHash(modelId: String, inputHash: String): Optional<DatasetPredictionCache>
}
