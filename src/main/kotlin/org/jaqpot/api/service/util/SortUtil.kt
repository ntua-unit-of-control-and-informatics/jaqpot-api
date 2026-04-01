package org.jaqpot.api.service.util

import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order

class SortUtil {
    companion object {
        private const val SORT_DELIMITER = "|" // Delimiter for sorting parameters

        fun parseSortParameters(sortParams: List<String>?): List<Order> {
            if (sortParams.isNullOrEmpty()) {
                return emptyList()
            }

            return sortParams
                .map { sortParam: String ->
                    val parts = sortParam.split(SORT_DELIMITER).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (parts.size == 2) {
                        val direction = Sort.Direction.fromString(parts[1])
                        val property = parts[0]
                        return@map if (direction == Sort.Direction.DESC) Order.desc(property) else Order.asc(property)
                    } else {
                        return@map Order.asc(parts[0])
                    }
                }
        }
    }
}
