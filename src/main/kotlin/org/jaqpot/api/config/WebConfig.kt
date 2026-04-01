package org.jaqpot.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun customSortArgumentResolver(): SortHandlerMethodArgumentResolver {
        return CustomSortArgumentResolver()
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        val sortResolver = customSortArgumentResolver() // Get the custom sort resolver
        val pageableResolver = PageableHandlerMethodArgumentResolver(sortResolver) // Create Pageable resolver with custom sort resolver
        resolvers.add(pageableResolver) // Add our custom Pageable resolver first
        // Spring will automatically add its default resolvers after this,
        // but our custom one will take precedence for Pageable arguments.
    }
}