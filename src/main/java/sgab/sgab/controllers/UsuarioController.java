package sgab.sgab.controllers;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sgab.sgab.Services.AdministradorService;
import sgab.sgab.Services.FuncionarioService;
import sgab.sgab.Services.LeitorService;
import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.response.LeitorResponseDTO;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping
public class UsuarioController{
    private final LeitorService leitorService;
    private final FuncionarioService funcionarioService;
    private final AdministradorService administradorService;

    public UsuarioController(LeitorService leitorService, FuncionarioService funcionarioService, AdministradorService administradorService){
        this.leitorService = leitorService;
        this.funcionarioService = funcionarioService;
        this.administradorService = administradorService;
    }

    @PostMapping("/cadastro/leitor")
    public ResponseEntity<LeitorResponseDTO> cadastroLeitor(@RequestBody @Valid LeitorRequestDTO dto) {
        LeitorResponseDTO response = leitorService.cadastrar(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}