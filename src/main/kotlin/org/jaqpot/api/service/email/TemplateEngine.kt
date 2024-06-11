package org.jaqpot.api.service.email

interface TemplateEngine<M, T> {
    fun convertToHTML(template: T, model: M?): String
}
