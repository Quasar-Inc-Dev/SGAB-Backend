package sgab.sgab.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgab.sgab.services.EmprestimoService;
import sgab.sgab.entities.Emprestimo;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }


    @GetMapping
    public ResponseEntity<List<Emprestimo>> listarTodos() {
        List<Emprestimo> emprestimos = emprestimoService.listarTodos();
        return ResponseEntity.ok(emprestimos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable Integer id) {
        Emprestimo emprestimo = emprestimoService.buscarPorId(id);
        return ResponseEntity.ok(emprestimo);
    }


    @PostMapping
    public ResponseEntity<Emprestimo> registrar(
            @RequestParam Integer exemplarId,
            @RequestParam Integer leitorId
    ) {
        Emprestimo novoEmprestimo = emprestimoService.registrarEmprestimo(exemplarId, leitorId);
        return ResponseEntity.status(201).body(novoEmprestimo);
    }


    @PatchMapping("/{id}/renovar")
    public ResponseEntity<Emprestimo> renovar(@PathVariable Integer id) {
        Emprestimo emprestimoRenovado = emprestimoService.renovar(id);
        return ResponseEntity.ok(emprestimoRenovado);
    }
}
