package sgab.sgab.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import sgab.sgab.services.ReservaService;
import sgab.sgab.dtos.response.ReservaResponseDTO;

@RestController
@RequestMapping("api/reservas")
@Tag(name = "Reserva", description = "Gerenciamento de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criarReserva(
            @RequestParam Integer livroId,
            @RequestParam Integer leitorId
    ) {
        ReservaResponseDTO novaReserva = reservaService.solicitarReserva(livroId, leitorId);
        return ResponseEntity.status(201).body(novaReserva);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        List<ReservaResponseDTO> reservas = reservaService.listarTodas();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Integer id) {
        ReservaResponseDTO reserva = reservaService.buscarPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Integer id) {
        ReservaResponseDTO reservaCancelada = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(reservaCancelada);
    }
}
