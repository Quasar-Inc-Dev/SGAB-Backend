package sgab.sgab.entities.valueobjects;

public record Cpf(String valor) {

    public Cpf {
        String limpo = valor == null ? "" : valor.replaceAll("[^0-9]", "");
        if (!validarCpf(limpo)) {
            throw new IllegalArgumentException("CPF inválido: " + valor);
        }
        valor = limpo;
    }

    private static boolean validarCpf(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        int primeiroDigito = 0;
        for (int i = 0; i < 9; i++) {
            primeiroDigito += Character.getNumericValue(cpf.charAt(i)) * (i + 1);
        }
        primeiroDigito %= 11;
        if (primeiroDigito == 10) primeiroDigito = 0;

        int segundoDigito = 0;
        for (int i = 0; i < 9; i++) {
            segundoDigito += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        segundoDigito += primeiroDigito * 2;
        segundoDigito = (segundoDigito * 10) % 11;
        if (segundoDigito == 10) segundoDigito = 0;

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    @Override
    public String toString() {
        return valor.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}