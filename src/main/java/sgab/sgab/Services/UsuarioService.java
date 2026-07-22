package sgab.sgab.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.Repositories.UsuarioRepository;
import sgab.sgab.dtos.request.UsuarioRequestDTO;
import sgab.sgab.dtos.response.UsuarioResponseDTO;
import sgab.sgab.entities.Usuario;
import sgab.sgab.exceptions.CpfDuplicadoExeception;
import sgab.sgab.exceptions.EmailDuplicadoException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto){
        validarDuplicidade(dto);

        Usuario usuario = new Usuario();

        usuario.setCpf(dto.cpf());
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setTipoUsuario(dto.tipoUsuario());
        usuario.setStatusUsuario(true);

        Usuario salvo = usuarioRepository.save(usuario);

        return toResponseDTO(salvo);
    }

    public void validarDuplicidade(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByCpf(dto.cpf())) {
            throw new CpfDuplicadoExeception("CPF já cadastrado!");
        }

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new EmailDuplicadoException("Email já cadastrado!");
        }
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getCpf(), usuario.getNome(), usuario.getEmail(), usuario.getStatusUsuario());
    }
}
