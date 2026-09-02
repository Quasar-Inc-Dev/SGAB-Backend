package sgab.sgab.controllers;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import sgab.sgab.Services.LeitorService;
import sgab.sgab.dtos.response.LeitorResponseDTO;
import sgab.sgab.entities.Leitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/leitor")
@Tag(name = "Leitores", description = "Gerenciamento de usuários leitores do sistema")
public class LeitorController {
    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @DeleteMapping("leitor/desativar/{id}")
    public ResponseEntity<Void> desativarLeitor(@PathVariable Integer id){
        leitorService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("leitor/buscar")
    public ResponseEntity<List<LeitorResponseDTO>> buscarLeitores() {
        List<LeitorResponseDTO> response = leitorService.listarTodos();
        return ResponseEntity.ok(response);
    }
    
}
