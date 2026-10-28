package sgab.sgab.entities.Enum;

public enum StatusSolicitacao {
    PENDENTE,
    // Empréstimo aprovado, com exemplar separado, esperando o leitor ir ao balcão
    AGUARDANDO_RETIRADA,
    APROVADA,
    RECUSADA,
    CANCELADA,
    // O leitor não retirou o exemplar dentro do prazo
    EXPIRADA
}
