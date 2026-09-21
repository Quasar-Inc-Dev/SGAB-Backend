package sgab.sgab.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sgab.sgab.Services.LeitorService;
import sgab.sgab.dtos.request.LeitorUpdateRequestDTO;
import sgab.sgab.dtos.response.LeitorResponseDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/leitor/leitor")
@Tag(name = "Leitores", description = "Gerenciamento de usuários leitores do sistema")
public class LeitorController {
    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<Void> desativarLeitor(@PathVariable Integer id){
        leitorService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LeitorResponseDTO>> buscarLeitores() {
        List<LeitorResponseDTO> response = leitorService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<LeitorResponseDTO> editarLeitor(@PathVariable Integer id, @RequestBody @Valid LeitorUpdateRequestDTO dto) {
        return ResponseEntity.ok(leitorService.editar(id, dto));
    }
    
}
