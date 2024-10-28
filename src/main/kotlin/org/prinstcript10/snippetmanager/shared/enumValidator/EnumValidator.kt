package org.prinstcript10.snippetmanager.shared.enumValidator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumFormat, String> {
    private lateinit var acceptedValues: List<String>

    override fun initialize(constraintAnnotation: EnumFormat) {
        acceptedValues = constraintAnnotation.enumClass.java.enumConstants.map { it.name }
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value == null || acceptedValues.contains(value)
    }
}
