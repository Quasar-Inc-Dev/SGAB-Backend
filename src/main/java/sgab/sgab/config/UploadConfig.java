package sgab.sgab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import sgab.sgab.services.ArmazenamentoImagemService;

/**
 * Serve os arquivos da pasta de upload como estáticos:
 * GET /upload/capas/livro-3-<uuid>.jpg -> <projeto>/upload/capas/livro-3-<uuid>.jpg
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

    private final ArmazenamentoImagemService armazenamentoImagemService;

    public UploadConfig(ArmazenamentoImagemService armazenamentoImagemService) {
        this.armazenamentoImagemService = armazenamentoImagemService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ArmazenamentoImagemService.PREFIXO_URL + "**")
                .addResourceLocations(armazenamentoImagemService.getDiretorioRaiz().toUri().toString());
    }
}
