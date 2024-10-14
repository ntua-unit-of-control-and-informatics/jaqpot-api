package org.jaqpot.api.service.model

import org.jaqpot.api.model.ModelTypeDto

fun ModelTypeDto.isQsarModel() = this in listOf(
    ModelTypeDto.QSAR_TOOLBOX_CALCULATOR,
    ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL,
    ModelTypeDto.QSAR_TOOLBOX_PROFILER
)
