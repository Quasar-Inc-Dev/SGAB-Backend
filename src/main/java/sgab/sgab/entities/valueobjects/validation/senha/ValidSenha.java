package sgab.sgab.entities.valueobjects.validation.senha;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SenhaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSenha {
    String message() default "A senha deve ter pelo menos 8 caracteres e conter letras maiúsculas, minúsculas, números e símbolos especiais.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
