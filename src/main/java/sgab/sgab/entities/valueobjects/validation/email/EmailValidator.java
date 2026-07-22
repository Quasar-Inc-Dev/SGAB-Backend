package sgab.sgab.entities.valueobjects.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String REGEX_EMAIL_INSTITUCIONAL =
            "^[\\w.+-]+@(aluno|professor|admin)\\.cps\\.sp\\.gov\\.br$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.trim().toLowerCase().matches(REGEX_EMAIL_INSTITUCIONAL);
    }
}