package sgab.sgab.controllers;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sgab.sgab.Services.AdministradorService;
import sgab.sgab.Services.FuncionarioService;
import sgab.sgab.Services.LeitorService;
import sgab.sgab.Services.UsuarioService;
import sgab.sgab.dtos.request.AdministradorRequestDTO;
import sgab.sgab.dtos.request.FuncionarioRequestDTO;
import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.request.UsuarioRequestDTO;
import sgab.sgab.dtos.response.AdministradorResponseDTO;
import sgab.sgab.dtos.response.CpfNaoEncontradoResponseDTO;
import sgab.sgab.dtos.response.FuncionarioResponseDTO;
import sgab.sgab.dtos.response.LeitorResponseDTO;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/usuario")
@Tag(name = "Usuários", description = "Gerenciamento de usuários no sistema")
public class UsuarioController{
    private final LeitorService leitorService;
    private final FuncionarioService funcionarioService;
    private final AdministradorService administradorService;
    private final UsuarioService usuarioService;

    public UsuarioController(LeitorService leitorService, FuncionarioService funcionarioService, 
        AdministradorService administradorService, UsuarioService usuarioService){
        this.leitorService = leitorService;
        this.funcionarioService = funcionarioService;
        this.administradorService = administradorService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("usuarios/cadastro/leitor")
    public ResponseEntity<LeitorResponseDTO> cadastroLeitor(@RequestBody @Valid LeitorRequestDTO dto) {
        LeitorResponseDTO response = leitorService.cadastrar(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("usuarios/cadastro/funcionario")
    public ResponseEntity<FuncionarioResponseDTO> cadastroFuncionario(@RequestBody @Valid FuncionarioRequestDTO dto){
        FuncionarioResponseDTO response = funcionarioService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("usuarios/cadastro/administrador")
    public ResponseEntity<AdministradorResponseDTO> cadastroAdm(@RequestBody @Valid AdministradorRequestDTO dto){
        AdministradorResponseDTO response = administradorService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @PostMapping("usuarios/buscarPorCpf")
    public ResponseEntity<CpfNaoEncontradoResponseDTO> buscarLeitorPorCPF(@RequestBody @Valid UsuarioRequestDTO request) {
        CpfNaoEncontradoResponseDTO response = usuarioService.buscarPorCpf(request.cpf());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}