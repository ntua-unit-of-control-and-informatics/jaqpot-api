package org.jaqpot.api.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.SortArgumentResolver
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class SpringDataWebConfig {

    @Bean
    fun pageableArgumentResolverPostProcessor(): BeanPostProcessor {
        return PageableArgumentResolverPostProcessor()
    }
}

class PageableArgumentResolverPostProcessor : BeanPostProcessor {
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is RequestMappingHandlerAdapter) {
            val customResolver = CustomPageableArgumentResolver()
            val existingResolvers = bean.customArgumentResolvers?.toMutableList() ?: mutableListOf()
            
            val pageableIndex = existingResolvers.indexOfFirst { it is PageableHandlerMethodArgumentResolver }
            if (pageableIndex >= 0) {
                existingResolvers[pageableIndex] = customResolver
            } else {
                existingResolvers.add(0, customResolver)
            }
            
            bean.customArgumentResolvers = existingResolvers
        }
        return bean
    }
}

@Configuration
class PageableConfig {
    @Bean
    fun pageableCustomizer(): PageableHandlerMethodArgumentResolverCustomizer {
        return PageableHandlerMethodArgumentResolverCustomizer { resolver ->
            resolver.setPageParameterName("page")
            resolver.setSizeParameterName("size")
        }
    }

    @Bean
    fun sortCustomizer(): SortHandlerMethodArgumentResolverCustomizer {
        return SortHandlerMethodArgumentResolverCustomizer { resolver ->
            resolver.setSortParameter("sort")
        }
    }
}

class CustomPageableArgumentResolver : HandlerMethodArgumentResolver {
    
    private val sortArgumentResolver = CustomSortArgumentResolver()

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Pageable::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val httpRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)
        if (httpRequest != null) {
            val page = httpRequest.getParameter("page")?.toIntOrNull() ?: 0
            val size = httpRequest.getParameter("size")?.toIntOrNull() ?: 10
            val sortParams = httpRequest.getParameterValues("sort")?.toList() ?: emptyList()
            
            val sort = sortArgumentResolver.parseSortParams(sortParams)
            return PageRequest.of(page, size, sort)
        }
        return PageRequest.of(0, 10)
    }
}

class CustomSortArgumentResolver : SortArgumentResolver {
    
    fun parseSortParams(sortParams: List<String>): Sort {
        if (sortParams.isEmpty()) {
            return Sort.unsorted()
        }

        val orders = mutableListOf<Sort.Order>()
        for (sortParam in sortParams) {
            val trimmed = sortParam.trim()
            if (trimmed.isEmpty()) {
                continue
            }

            val direction: Sort.Direction
            val propertyName: String

            val pipeIndex = trimmed.indexOf('|')
            if (pipeIndex > 0) {
                val directionPart = trimmed.substring(pipeIndex + 1).trim()
                propertyName = trimmed.substring(0, pipeIndex).trim()
                direction = try {
                    Sort.Direction.fromString(directionPart)
                } catch (ex: IllegalArgumentException) {
                    Sort.DEFAULT_DIRECTION
                }
            } else {
                propertyName = trimmed
                direction = Sort.DEFAULT_DIRECTION
            }

            orders.add(if (direction == Sort.Direction.DESC) {
                Sort.Order.desc(propertyName)
            } else {
                Sort.Order.asc(propertyName)
            })
        }

        return Sort.by(orders)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Sort::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Sort {
        val httpRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)
        val sortParams = httpRequest?.getParameterValues("sort")?.toList() ?: emptyList()
        return parseSortParams(sortParams)
    }
}
