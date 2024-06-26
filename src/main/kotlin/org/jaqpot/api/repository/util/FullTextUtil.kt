package org.jaqpot.api.repository.util

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
    }

}
