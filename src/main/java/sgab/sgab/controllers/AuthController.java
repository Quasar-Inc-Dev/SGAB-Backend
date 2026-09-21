package sgab.sgab.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sgab.sgab.dtos.request.LoginRequestDTO;
import sgab.sgab.dtos.response.LoginResponseDTO;
import sgab.sgab.security.JwtService;
import sgab.sgab.security.UsuarioDetails;

@RestController 
@Tag(name = "Login", description = "Autenticação e geração de token para o acesso no sistema")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        UsuarioDetails usuarioDetails = (UsuarioDetails) auth.getPrincipal();
        String token = jwtService.gerarToken(usuarioDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
