package sgab.sgab.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


import sgab.sgab.dtos.response.ErroResponseDTO;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LivroNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratarLivroNaoEncontrado(
            LivroNaoEncontradoException exception
    ) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception
    ) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> tratarConcorrencia(
            ObjectOptimisticLockingFailureException exception
    ) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("erro", "O exemplar foi alterado por outra operação. Tente novamente.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarArgumentoInvalido(
            IllegalArgumentException exception
    ) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> resposta = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        resposta.put(erro.getField(), erro.getDefaultMessage())
                );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(CpfNaoEncontrado.class)
    public ResponseEntity<ErroResponseDTO> handleNaoEncontrado(CpfNaoEncontrado e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({CpfDuplicadoExeception.class, EmailDuplicadoException.class})
    public ResponseEntity<ErroResponseDTO> handlConflito(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tratarArquivoGrande(MaxUploadSizeExceededException exception) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("erro", "A imagem é maior que o tamanho máximo permitido (5 MB)");

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(resposta);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponseDTO> handleLoginInvalido(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroResponseDTO("Email ou senha inválidos"));
    }
}
