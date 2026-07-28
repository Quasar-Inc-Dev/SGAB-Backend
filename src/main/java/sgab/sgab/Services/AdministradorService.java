package sgab.sgab.Services;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.Repositories.AdministradorRepository;
import sgab.sgab.dtos.request.AdministradorRequestDTO;
import sgab.sgab.dtos.response.AdministradorResponseDTO;
import sgab.sgab.entities.Administrador;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;

@Service
public class AdministradorService {
    private final UsuarioService usuarioService;
    private final AdministradorRepository adminRepository;

    public AdministradorService(UsuarioService usuarioService, AdministradorRepository adminRepository){
        this.usuarioService = usuarioService;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public AdministradorResponseDTO cadastrar(AdministradorRequestDTO dto) {
        Usuario usuario = usuarioService.criarUsuarioBase(dto.cpf(), 
       dto.nome(), dto.email(), dto.senha(), TipoUsuario.ADMINISTRADOR);

        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setStatusAdministrador(true);

        Administrador salvo = adminRepository.save(administrador);

        return new AdministradorResponseDTO(
            usuario.getCpf(), usuario.getNome(), usuario.getEmail(),
            salvo.getStatusAdministrador()
        );
    }
}
