package org.jaqpot.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig {

    @Bean
    fun customSortArgumentResolver(): SortHandlerMethodArgumentResolver {
        return CustomSortArgumentResolver()
    }

    @Bean
    fun pageableResolver(sortResolver: SortHandlerMethodArgumentResolver): PageableHandlerMethodArgumentResolver {
        val resolver = PageableHandlerMethodArgumentResolver(sortResolver)
        // You can configure other properties of the PageableHandlerMethodArgumentResolver here if needed
        // For example, resolver.setFallbackPageable(PageRequest.of(0, 20));
        return resolver
    }
}
