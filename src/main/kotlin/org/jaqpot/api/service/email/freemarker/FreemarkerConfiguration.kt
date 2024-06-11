package org.jaqpot.api.service.email.freemarker

import freemarker.cache.ClassTemplateLoader
import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer

class FreemarkerConfiguration {
    @Bean
    fun freemarkerClassLoaderConfig(): FreeMarkerConfigurer {
        val configuration: Configuration = Configuration(Configuration.VERSION_2_3_33)
        val templateLoader: TemplateLoader = ClassTemplateLoader(this.javaClass, "/mail-templates")
        configuration.templateLoader = templateLoader
        val freeMarkerConfigurer = FreeMarkerConfigurer()
        freeMarkerConfigurer.configuration = configuration
        return freeMarkerConfigurer
    }

}
