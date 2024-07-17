package org.jaqpot.api.repository

import org.jaqpot.api.entity.Feature
import org.springframework.data.repository.CrudRepository

interface FeatureRepository : CrudRepository<Feature, Long>
