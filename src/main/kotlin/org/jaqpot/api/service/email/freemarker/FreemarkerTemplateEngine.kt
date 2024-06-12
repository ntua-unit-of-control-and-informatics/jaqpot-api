package org.jaqpot.api.service.email.freemarker

import freemarker.template.Template
import org.jaqpot.api.service.email.TemplateEngine
import org.springframework.stereotype.Component
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer


@Component
class FreemarkerTemplateEngine(private val freemarkerConfigurer: FreeMarkerConfigurer) :
    TemplateEngine<Map<String, Any>, FreemarkerTemplate> {
    override fun convertToHTML(template: FreemarkerTemplate, model: Map<String, Any>?): String {
        val freemarkerTemplate: Template = freemarkerConfigurer.configuration
            .getTemplate(template.path)
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model)
    }
}
