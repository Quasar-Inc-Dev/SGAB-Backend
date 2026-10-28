package sgab.sgab.entities.valueobjects;

import jakarta.persistence.Embeddable;
import sgab.sgab.entities.valueobjects.validation.senha.ValidSenha;
import java.util.Objects;

@Embeddable
public class Senha {

    @ValidSenha
    private String valor;

    protected Senha() {}

    public Senha(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Senha senha = (Senha) o;
        return Objects.equals(valor, senha.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return "********";
    }
}
