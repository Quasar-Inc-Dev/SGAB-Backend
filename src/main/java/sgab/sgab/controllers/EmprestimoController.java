package sgab.sgab.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgab.sgab.services.EmprestimoService;
import sgab.sgab.dtos.response.EmprestimoResponseDTO;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    // GET /emprestimos
    @GetMapping
    public ResponseEntity<List<EmprestimoResponseDTO>> listarTodos() {
        List<EmprestimoResponseDTO> emprestimos = emprestimoService.listarTodos();
        return ResponseEntity.ok(emprestimos);
    }

    // GET /emprestimos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoResponseDTO> buscarPorId(@PathVariable Integer id) {
        EmprestimoResponseDTO emprestimo = emprestimoService.buscarPorId(id);
        return ResponseEntity.ok(emprestimo);
    }

    // POST /emprestimos
    @PostMapping
    public ResponseEntity<EmprestimoResponseDTO> registrar(
            @RequestParam Integer exemplarId,
            @RequestParam Integer leitorId
    ) {
        EmprestimoResponseDTO novoEmprestimo = emprestimoService.registrarEmprestimo(exemplarId, leitorId);
        return ResponseEntity.status(201).body(novoEmprestimo);
    }

    // PATCH /emprestimos/{id}/renovar
    @PatchMapping("/{id}/renovar")
    public ResponseEntity<EmprestimoResponseDTO> renovar(@PathVariable Integer id) {
        EmprestimoResponseDTO emprestimoRenovado = emprestimoService.renovar(id);
        return ResponseEntity.ok(emprestimoRenovado);
    }
}
