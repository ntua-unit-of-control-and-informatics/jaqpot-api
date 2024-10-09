package org.jaqpot.api.service.model.constraints

import jakarta.validation.Constraint
import jakarta.validation.Payload
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import kotlin.reflect.KClass

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [DoaValidator::class])
annotation class ValidDoa(
    val message: String = "Doa is not valid. {message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
