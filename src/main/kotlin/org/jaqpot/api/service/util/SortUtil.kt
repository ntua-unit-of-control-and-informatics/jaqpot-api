package org.jaqpot.api.service.util

import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order

class SortUtil {
    companion object {
        fun parseSortParameters(sortParams: List<String>?): List<Order> {
            if (sortParams.isNullOrEmpty()) {
                return listOf()
            }

            return sortParams
                .map { sortParam: String ->
                    val parts = sortParam.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (parts.size == 2) {
                        return@map Order(Sort.Direction.fromString(parts[1]), parts[0])
                    } else {
                        return@map Order(Sort.Direction.ASC, parts[0]) // Default to ascending
                    }
                }
        }
    }
}
