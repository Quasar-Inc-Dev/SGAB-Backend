package sgab.sgab.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgab.sgab.services.ReservaService;
import sgab.sgab.dtos.response.ReservaResponseDTO;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // POST /reservas
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criarReserva(
            @RequestParam Integer exemplarId,
            @RequestParam Integer leitorId
    ) {
        ReservaResponseDTO novaReserva = reservaService.solicitarReserva(exemplarId, leitorId);
        return ResponseEntity.status(201).body(novaReserva);
    }

    // GET /reservas
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        List<ReservaResponseDTO> reservas = reservaService.listarTodas();
        return ResponseEntity.ok(reservas);
    }

    // GET /reservas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Integer id) {
        ReservaResponseDTO reserva = reservaService.buscarPorId(id);
        return ResponseEntity.ok(reserva);
    }

    // DELETE /reservas/{id} ou PATCH para cancelar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Integer id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }
}
