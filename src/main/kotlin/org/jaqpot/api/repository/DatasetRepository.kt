package org.jaqpot.api.repository

import org.jaqpot.api.entity.Dataset
import org.springframework.data.repository.CrudRepository

interface DatasetRepository : CrudRepository<Dataset, Long>
