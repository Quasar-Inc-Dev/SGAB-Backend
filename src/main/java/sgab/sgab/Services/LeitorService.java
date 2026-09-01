package sgab.sgab.Services;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sgab.sgab.Repositories.LeitorRepository;
import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.response.LeitorResponseDTO;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;
import sgab.sgab.exceptions.CpfNaoEncontrado;

@Service
public class LeitorService {
    private final UsuarioService usuarioService;
    private final LeitorRepository leitorRepository;

    public LeitorService(UsuarioService usuarioService, LeitorRepository leitorRepository){
        this.usuarioService = usuarioService;
        this.leitorRepository = leitorRepository;
    }

        @Transactional
        public LeitorResponseDTO cadastrar(LeitorRequestDTO dto) {
        Usuario usuario = usuarioService.criarUsuarioBase(
                dto.cpf(), dto.nome(), dto.email(), dto.senha(), TipoUsuario.LEITOR
        );

        Leitor leitor = new Leitor();
        leitor.setUsuario(usuario);
        leitor.setGenero(dto.genero());
        leitor.setDataNascimento(dto.dataNascimento());
        leitor.setTipoLeitor(dto.tipoLeitor());
        leitor.setCpf(dto.cpf());
        leitor.setStatusLeitor(true);

        Leitor salvo = leitorRepository.save(leitor);

        return new LeitorResponseDTO(
                usuario.getId(), usuario.getCpf(), usuario.getNome(), usuario.getEmail(),
                salvo.getGenero(), salvo.getDataNascimento(), salvo.getTipoLeitor(), salvo.getStatusLeitor()
        );
    }

    @Transactional
        public void desativar(Integer id) {
            Leitor leitor = leitorRepository.findById(id)
                .orElseThrow(() -> new CpfNaoEncontrado("Usuário não encotrado na base de dados!"));

            leitor.setStatusLeitor(false);
            leitorRepository.save(leitor);

            usuarioService.desativarUsuario(leitor.getId());
    }
}
