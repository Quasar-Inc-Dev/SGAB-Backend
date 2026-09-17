package sgab.sgab.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sgab.sgab.Services.LivroIsbnService;
import sgab.sgab.Services.LivroService;
import sgab.sgab.dtos.request.LivroRequestDTO;
import sgab.sgab.dtos.response.LivroIsbnResponseDTO;
import sgab.sgab.dtos.response.LivroResponseDTO;

@RestController
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "Gerenciamento de livros")
public class LivroController {

    private final LivroService livroService;
    private final LivroIsbnService livroIsbnService;

    public LivroController(
            LivroService livroService,
            LivroIsbnService livroIsbnService
    ) {
        this.livroService = livroService;
        this.livroIsbnService = livroIsbnService;
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<LivroIsbnResponseDTO> consultarIsbn(
            @PathVariable String isbn
    ) {
        LivroIsbnResponseDTO response =
                livroIsbnService.consultarPorIsbn(isbn);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LivroResponseDTO> cadastrar(
            @RequestBody @Valid LivroRequestDTO dto
    ) {
        LivroResponseDTO response = livroService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LivroResponseDTO>> listarTodos() {
        return ResponseEntity.ok(livroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> buscarPorId(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(livroService.buscarPorId(id));
    }

    @GetMapping("/buscar-por-isbn/{isbn}")
    public ResponseEntity<LivroResponseDTO> buscarPorIsbn(
            @PathVariable String isbn
    ) {
        return ResponseEntity.ok(livroService.buscarPorIsbn(isbn));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid LivroRequestDTO dto
    ) {
        return ResponseEntity.ok(livroService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Integer id
    ) {
        livroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
