package sgab.sgab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CpfNaoEncontrado extends RuntimeException {
    public CpfNaoEncontrado(String message){
        super(message);
    }
}
