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
import sgab.sgab.dtos.request.EsqueciSenhaRequestDTO;
import sgab.sgab.dtos.request.LoginRequestDTO;
import sgab.sgab.dtos.request.RedefinirSenhaRequestDTO;
import sgab.sgab.dtos.response.ErroResponseDTO;
import sgab.sgab.dtos.response.LoginResponseDTO;
import sgab.sgab.security.JwtService;
import sgab.sgab.security.UsuarioDetails;
import sgab.sgab.services.RedefinicaoSenhaService;

@RestController 
@Tag(name = "Login", description = "Autenticação e geração de token para o acesso no sistema")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedefinicaoSenhaService redefinicaoSenhaService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
            RedefinicaoSenhaService redefinicaoSenhaService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redefinicaoSenhaService = redefinicaoSenhaService;
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

    // Sempre responde igual, exista ou não o e-mail, para não revelar quem tem conta
    @PostMapping("/esqueci-senha")
    public ResponseEntity<ErroResponseDTO> esqueciSenha(@RequestBody @Valid EsqueciSenhaRequestDTO dto) {
        redefinicaoSenhaService.solicitar(dto.email());
        return ResponseEntity.ok(new ErroResponseDTO(
            "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<ErroResponseDTO> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequestDTO dto) {
        redefinicaoSenhaService.redefinir(dto.token(), dto.novaSenha());
        return ResponseEntity.ok(new ErroResponseDTO("Senha redefinida com sucesso."));
    }
}
