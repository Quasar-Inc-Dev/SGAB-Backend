package sgab.sgab.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import sgab.sgab.services.EmprestimoService;
import sgab.sgab.dtos.response.EmprestimoResponseDTO;

@RestController
@RequestMapping("api/emprestimos")
@Tag(name = "Emprestimo", description = "Gerenciamento de emprestimo")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }


    @GetMapping
    public ResponseEntity<List<EmprestimoResponseDTO>> listarTodos() {
        List<EmprestimoResponseDTO> emprestimos = emprestimoService.listarTodos();
        return ResponseEntity.ok(emprestimos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoResponseDTO> buscarPorId(@PathVariable Integer id) {
        EmprestimoResponseDTO emprestimo = emprestimoService.buscarPorId(id);
        return ResponseEntity.ok(emprestimo);
    }


    // O exemplar é escolhido pelo sistema: o separado por reserva do leitor ou o primeiro disponível
    @PostMapping
    public ResponseEntity<EmprestimoResponseDTO> registrar(
            @RequestParam Integer livroId,
            @RequestParam Integer leitorId
    ) {
        EmprestimoResponseDTO novoEmprestimo = emprestimoService.registrarEmprestimo(livroId, leitorId);
        return ResponseEntity.status(201).body(novoEmprestimo);
    }


    @PatchMapping("/{id}/renovar")
    public ResponseEntity<EmprestimoResponseDTO> renovar(@PathVariable Integer id) {
        EmprestimoResponseDTO emprestimoRenovado = emprestimoService.renovar(id);
        return ResponseEntity.ok(emprestimoRenovado);
    }


    @PatchMapping("/{id}/devolver")
    public ResponseEntity<EmprestimoResponseDTO> devolver(@PathVariable Integer id) {
        EmprestimoResponseDTO emprestimoDevolvido = emprestimoService.devolver(id);
        return ResponseEntity.ok(emprestimoDevolvido);
    }
}
