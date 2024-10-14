package org.jaqpot.api.service.model

import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelType

fun Model.isQsarModel() = this.type in listOf(
    ModelType.QSAR_TOOLBOX_CALCULATOR,
    ModelType.QSAR_TOOLBOX_QSAR_MODEL,
    ModelType.QSAR_TOOLBOX_PROFILER
)
