package org.jaqpot.api.config

import org.springframework.core.MethodParameter
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer

class CustomSortArgumentResolver : SortHandlerMethodArgumentResolver() {

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Sort { // Changed return type to Sort
        val sortParameters = webRequest.getParameterValues(super.getSortParameter(parameter)) ?: return Sort.unsorted()

        val orders = sortParameters.mapNotNull { singleSort ->
            val parts = singleSort.split("|")
            if (parts.size == 2) {
                val property = parts[0]
                val direction = Sort.Direction.fromString(parts[1])
                Sort.Order(direction, property)
            } else {
                null // Ignore malformed sort parameters
            }
        }
        return if (orders.isNotEmpty()) Sort.by(orders) else Sort.unsorted()
    }
}