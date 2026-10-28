package sgab.sgab.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sgab.sgab.dtos.request.RecusaSolicitacaoRequestDTO;
import sgab.sgab.dtos.response.SolicitacaoResponseDTO;
import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;
import sgab.sgab.security.UsuarioDetails;
import sgab.sgab.services.SolicitacaoService;

@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações", description = "Pedidos de empréstimo e reserva feitos pelo leitor e analisados pelo funcionário")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    // ---- Leitor ----

    @PostMapping("/emprestimo")
    public ResponseEntity<SolicitacaoResponseDTO> solicitarEmprestimo(
            @RequestParam Integer livroId,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        SolicitacaoResponseDTO response = solicitacaoService.solicitarEmprestimo(livroId, usuarioDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reserva")
    public ResponseEntity<SolicitacaoResponseDTO> solicitarReserva(
            @RequestParam Integer exemplarId,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        SolicitacaoResponseDTO response = solicitacaoService.solicitarReserva(exemplarId, usuarioDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarMinhas(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return ResponseEntity.ok(solicitacaoService.listarDoLeitor(usuarioDetails.getUsuario()));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SolicitacaoResponseDTO> cancelar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return ResponseEntity.ok(solicitacaoService.cancelar(id, usuarioDetails.getUsuario()));
    }

    // ---- Funcionário / Administrador ----

    @GetMapping
    public ResponseEntity<List<SolicitacaoResponseDTO>> listar(
            @RequestParam(defaultValue = "PENDENTE") StatusSolicitacao status,
            @RequestParam(required = false) TipoSolicitacao tipo
    ) {
        return ResponseEntity.ok(solicitacaoService.listarPorStatus(status, tipo));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoResponseDTO> aprovar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return ResponseEntity.ok(solicitacaoService.aprovar(id, usuarioDetails.getUsuario()));
    }

    @PatchMapping("/{id}/confirmar-retirada")
    public ResponseEntity<SolicitacaoResponseDTO> confirmarRetirada(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return ResponseEntity.ok(solicitacaoService.confirmarRetirada(id, usuarioDetails.getUsuario()));
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<SolicitacaoResponseDTO> recusar(
            @PathVariable Integer id,
            @RequestBody(required = false) @Valid RecusaSolicitacaoRequestDTO dto,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        String motivo = dto == null ? null : dto.motivo();
        return ResponseEntity.ok(solicitacaoService.recusar(id, usuarioDetails.getUsuario(), motivo));
    }
}
