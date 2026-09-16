package sgab.sgab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sgab.sgab.dtos.response.ErroResponseDTO;

@RestControllerAdvice 
public class ApiExceptionHandler {

    @ExceptionHandler(CpfNaoEncontrado.class)
    public ResponseEntity<ErroResponseDTO> handleNaoEncontrado(CpfNaoEncontrado e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({CpfDuplicadoExeception.class, EmailDuplicadoException.class})
    public ResponseEntity<ErroResponseDTO> handlConflito(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponseDTO> handleLoginInvalido(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroResponseDTO("Email ou senha inválidos"));
    }
}
