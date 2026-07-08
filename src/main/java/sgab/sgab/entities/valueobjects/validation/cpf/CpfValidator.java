package sgab.sgab.entities.valueobjects.validation.cpf;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) return false;
        return validarCpf(cpf);
    }

    private boolean validarCpf(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int primeiroDigito = 0;
        for (int i = 0; i < 9; i++) {
            primeiroDigito += Character.getNumericValue(cpf.charAt(i)) * (i + 1);
        }
        primeiroDigito = primeiroDigito % 11;
        if (primeiroDigito == 10) primeiroDigito = 0;

        int segundoDigito = 0;
        for (int i = 0; i < 9; i++) {
            segundoDigito += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        segundoDigito += primeiroDigito * 2;
        segundoDigito = (segundoDigito * 10) % 11;
        if (segundoDigito == 10) segundoDigito = 0;

        int digitoVerificador1 = Character.getNumericValue(cpf.charAt(9));
        int digitoVerificador2 = Character.getNumericValue(cpf.charAt(10));

        return primeiroDigito == digitoVerificador1 && segundoDigito == digitoVerificador2;
    }
}