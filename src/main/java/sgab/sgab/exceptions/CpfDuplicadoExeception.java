package sgab.sgab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CpfDuplicadoExeception extends RuntimeException{
    
    public CpfDuplicadoExeception(String message){
        super(message);
    }
}
