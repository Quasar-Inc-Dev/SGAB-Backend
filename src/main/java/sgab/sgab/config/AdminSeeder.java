package sgab.sgab.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import sgab.sgab.Repositories.UsuarioRepository;
import sgab.sgab.Services.AdministradorService;
import sgab.sgab.dtos.request.AdministradorRequestDTO;

@Component 
public class AdminSeeder implements CommandLineRunner {
    private final AdministradorService administradorService;
    private final UsuarioRepository usuarioRepository;

    public AdminSeeder(AdministradorService administradorService, UsuarioRepository usuarioRepository){
        this.administradorService = administradorService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override 
    public void run(String... args) {
        String email = "admin@admin.cps.sp.gov.br";
        if (usuarioRepository.existsByEmail(email)) return;

        administradorService.cadastrar(new AdministradorRequestDTO("52998224725", "Admin", email, "FsHm2026"));
    }
}
