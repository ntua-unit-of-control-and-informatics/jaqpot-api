package org.jaqpot.api.repository.util

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class FullTextUtil {
    companion object {
        /**
         * Transforms a string of separated words to the full text representation with | so that not the whole phrase
         * is searched in the database but each word with an OR
         */
        fun transformSearchQuery(query: String): String {
            return query.trim().split("\\s+".toRegex())
                .joinToString(" | ") { it.trim() }
        }

        fun transformPageableForNative(pageable: Pageable): Pageable {
            if (pageable.sort.isUnsorted) return pageable
            val orders = pageable.sort.map { order ->
                val snakeProperty = order.property.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
                Sort.Order(order.direction, snakeProperty)
            }.toList()
            return PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(orders))
        }
    }

}
