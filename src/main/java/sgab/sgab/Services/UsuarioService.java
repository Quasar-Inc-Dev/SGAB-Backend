package sgab.sgab.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.Repositories.UsuarioRepository;
import sgab.sgab.dtos.response.CpfNaoEncontradoResponseDTO;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;
import sgab.sgab.exceptions.CpfDuplicadoExeception;
import sgab.sgab.exceptions.CpfNaoEncontrado;
import sgab.sgab.exceptions.EmailDuplicadoException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, 
        PasswordEncoder passwordEncoder 
        ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    protected Usuario criarUsuarioBase(String cpf, String nome, String email, String senha, TipoUsuario tipo) {
        validarDuplicidade(cpf, email);

        Usuario usuario = new Usuario();
        usuario.setCpf(cpf);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setTipoUsuario(tipo);
        usuario.setStatusUsuario(true);

        return usuarioRepository.save(usuario);
    }

    public CpfNaoEncontradoResponseDTO buscarPorCpf(String cpf) {
        Usuario usuario = usuarioRepository.findByCpf(cpf)
            .orElseThrow(() -> new CpfNaoEncontrado("CPF de usuário não encontrado na base de dados!"));

        return new CpfNaoEncontradoResponseDTO(
            usuario.getId(), usuario.getCpf(), usuario.getNome(), 
            usuario.getEmail(), usuario.getTipoUsuario(), usuario.getStatusUsuario()
        );
    } 

    @Transactional
    public void desativarUsuario(Integer id){
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new CpfNaoEncontrado("Usuário não encontrado"));

        usuario.setStatusUsuario(false);
        usuarioRepository.save(usuario);
    }

    public void validarDuplicidade(String cpf, String email) {
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new CpfDuplicadoExeception("CPF já cadastrado!");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException("Email já cadastrado!");
        }
    }
}
