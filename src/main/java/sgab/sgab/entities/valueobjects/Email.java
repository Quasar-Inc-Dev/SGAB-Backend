package sgab.sgab.entities.valueobjects;

public record Email(String endereco) {

    private static final String REGEX_EMAIL_INSTITUCIONAL =
            "^[\\w.+-]+@(aluno|professor|admin)\\.cps\\.gov\\.br$";

    public Email {
        if (endereco == null || endereco.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        String normalizado = endereco.trim().toLowerCase();

        if (!normalizado.matches(REGEX_EMAIL_INSTITUCIONAL)) {
            throw new IllegalArgumentException(
                    "Email deve ser institucional, no formato usuario@(aluno|professor|admin).cps.gov.br: " + endereco
            );
        }

        endereco = normalizado;
    }
}