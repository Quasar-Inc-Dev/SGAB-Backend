package sgab.sgab.controllers;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sgab.sgab.Services.LeitorService;

@RestController
@RequestMapping
public class LeitorController {
    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @DeleteMapping("usuario/deletar/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id){
        leitorService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
