package sgab.sgab.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.events.UsuarioCadastradoEvent;
import sgab.sgab.repositories.AdministradorRepository;
import sgab.sgab.dtos.request.AdministradorRequestDTO;
import sgab.sgab.dtos.response.AdministradorResponseDTO;
import sgab.sgab.entities.Administrador;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;

@Service
public class AdministradorService {
    private final UsuarioService usuarioService;
    private final AdministradorRepository adminRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdministradorService(UsuarioService usuarioService, AdministradorRepository adminRepository,
            ApplicationEventPublisher eventPublisher){
        this.usuarioService = usuarioService;
        this.adminRepository = adminRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AdministradorResponseDTO cadastrar(AdministradorRequestDTO dto) {
        return cadastrar(dto, true);
    }

    // notificarPorEmail = false é usado pelo AdminSeeder, para não disparar e-mail a cada inicialização
    @Transactional
    public AdministradorResponseDTO cadastrar(AdministradorRequestDTO dto, boolean notificarPorEmail) {
        Usuario usuario = usuarioService.criarUsuarioBase(dto.cpf(),
       dto.nome(), dto.email(), dto.senha(), TipoUsuario.ADMINISTRADOR);

        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setCpf(dto.cpf());
        administrador.setStatusAdministrador(true);

        Administrador salvo = adminRepository.save(administrador);

        if (notificarPorEmail) {
            eventPublisher.publishEvent(new UsuarioCadastradoEvent(usuario.getEmail(), usuario.getNome()));
        }

        return new AdministradorResponseDTO(
            usuario.getId(), usuario.getCpf(), usuario.getNome(), usuario.getEmail(),
            salvo.getStatusAdministrador()
        );
    }
}
