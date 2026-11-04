package sgab.sgab.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.events.UsuarioCadastradoEvent;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.request.LeitorUpdateRequestDTO;
import sgab.sgab.dtos.response.LeitorResponseDTO;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.TipoUsuario;
import sgab.sgab.exceptions.CpfNaoEncontrado;

@Service
public class LeitorService {
    private final UsuarioService usuarioService;
    private final LeitorRepository leitorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LeitorService(UsuarioService usuarioService, LeitorRepository leitorRepository,
            ApplicationEventPublisher eventPublisher){
        this.usuarioService = usuarioService;
        this.leitorRepository = leitorRepository;
        this.eventPublisher = eventPublisher;
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

        // O e-mail só sai depois do commit (ver NotificacaoEmailListener)
        eventPublisher.publishEvent(new UsuarioCadastradoEvent(usuario.getEmail(), usuario.getNome()));

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

    @Transactional(readOnly = true)
    public Page<LeitorResponseDTO> listar(Pageable pageable) {
        return leitorRepository.findAll(pageable)
            .map(leitor -> new LeitorResponseDTO(
                leitor.getUsuario().getId(), leitor.getCpf(),
                leitor.getUsuario().getNome(), leitor.getUsuario().getEmail(),
                leitor.getGenero(), leitor.getDataNascimento(),
                leitor.getTipoLeitor(), leitor.getStatusLeitor()
            ));
    }

    @Transactional
    public LeitorResponseDTO editar(Integer id, LeitorUpdateRequestDTO dto) {
        Leitor leitor = leitorRepository.findById(id)
            .orElseThrow(() -> new CpfNaoEncontrado("Leitor não encontrado"));

        usuarioService.atualizarUsuarioBase(leitor.getUsuario(), dto.nome());

        leitor.setGenero(dto.genero());
        leitor.setDataNascimento(dto.dataNascimento());
        leitor.setTipoLeitor(dto.tipoLeitor());

        return new LeitorResponseDTO(
            leitor.getUsuario().getId(), leitor.getCpf(),
            leitor.getUsuario().getNome(), leitor.getUsuario().getEmail(),
            leitor.getGenero(), leitor.getDataNascimento(),
            leitor.getTipoLeitor(), leitor.getStatusLeitor()
        );
    }
}
