package sgab.sgab.exceptions;

/** Falha ao gravar arquivos no armazenamento externo (Supabase Storage). */
public class ArmazenamentoException extends RuntimeException {

    public ArmazenamentoException(String mensagem) {
        super(mensagem);
    }
}
