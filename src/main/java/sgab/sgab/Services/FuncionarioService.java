package sgab.sgab.Services;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.Repositories.FuncionarioRepository;
import sgab.sgab.dtos.request.FuncionarioRequestDTO;
import sgab.sgab.dtos.response.FuncionarioResponseDTO;
import sgab.sgab.entities.Funcionario;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;

@Service
public class FuncionarioService {
    private final UsuarioService usuarioService;
    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(UsuarioService usuarioService, FuncionarioRepository funcionarioRepository) {
        this.usuarioService = usuarioService;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public FuncionarioResponseDTO cadastrar(FuncionarioRequestDTO dto) {
        Usuario usuario = usuarioService.criarUsuarioBase(dto.cpf(), dto.nome(), dto.email(), dto.senha(), TipoUsuario.FUNCIONARIO);

        Funcionario funcionario = new Funcionario();
        funcionario.setUsuario(usuario);
        funcionario.setMatricula(dto.matricula());
        funcionario.setDataContratacao(dto.dataContratacao());
        funcionario.setCpf(dto.cpf());
        funcionario.setStatusFuncionario(true);

        Funcionario salvo = funcionarioRepository.save(funcionario);

        return new FuncionarioResponseDTO(
            usuario.getId(), usuario.getCpf(), usuario.getNome(), usuario.getEmail(),
            salvo.getMatricula(), salvo.getDataContratacao(), salvo.getStatusFuncionario()
        );
    }
}
