package it.polito.wa2.login_service.annotations

import java.util.stream.Collectors
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.LengthRule
import org.passay.PasswordData
import org.passay.PasswordValidator
import org.passay.RuleResult
import org.passay.WhitespaceRule

class PasswordConstraintValidator : ConstraintValidator<ValidPassword?, String?> {

    override fun initialize(arg0: ValidPassword?) {}

    override fun isValid(password: String?, context: ConstraintValidatorContext): Boolean {
        val validator = PasswordValidator(
            listOf(
                // at least 8 characters
                LengthRule(8, 30),
                // at least one upper-case character
                CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                CharacterRule(EnglishCharacterData.Special, 1),
                // no whitespace
                WhitespaceRule()
            )
        )
        val result: RuleResult = validator.validate(PasswordData(password))
        if (result.isValid) {
            return true
        }
        val messages: List<String> = validator.getMessages(result)
        val messageTemplate = messages.stream().collect(Collectors.joining(","))

        context.buildConstraintViolationWithTemplate(messageTemplate)
            .addConstraintViolation()
            .disableDefaultConstraintViolation()
        return false
    }
}
