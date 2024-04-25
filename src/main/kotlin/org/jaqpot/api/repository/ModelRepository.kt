package org.jaqpot.api.repository

import org.jaqpot.api.entity.Model
import org.springframework.data.repository.CrudRepository

interface ModelRepository : CrudRepository<Model, Int>
