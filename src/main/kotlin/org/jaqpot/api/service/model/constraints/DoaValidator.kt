package org.jaqpot.api.service.model.constraints

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl
import org.jaqpot.api.model.DoaDto

class DoaValidator : ConstraintValidator<ValidDoa, List<DoaDto>?> {
    override fun isValid(value: List<DoaDto>?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrEmpty()) return true

        value.forEach {
            if (it.method == DoaDto.Method.LEVERAGE) {
                if (it.data.hStar == null) {
                    context?.let { constraintValidatorContext ->
                        (constraintValidatorContext as ConstraintValidatorContextImpl).addMessageParameter(
                            "message",
                            "hStar for leverage is required"
                        )
                    }
                    return false
                }

                if (it.data.doaMatrix == null) {
                    context?.let { constraintValidatorContext ->
                        (constraintValidatorContext as ConstraintValidatorContextImpl).addMessageParameter(
                            "message",
                            "array for leverage is required"
                        )
                    }
                    return false
                }

            }
        }


        return true
    }
}
