package sgab.sgab.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Grava as imagens na pasta de upload do projeto e devolve o caminho público
 * (ex.: /upload/capas/livro-3-<uuid>.jpg), que é o valor salvo no imgUrl do livro.
 */
@Service
public class ArmazenamentoImagemService {

    public static final String PREFIXO_URL = "/upload/";
    private static final String PASTA_CAPAS = "capas";

    private final Path diretorioRaiz;

    public ArmazenamentoImagemService(@Value("${sgab.upload.diretorio:upload}") String diretorio) {
        this.diretorioRaiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public Path getDiretorioRaiz() {
        return diretorioRaiz;
    }

    public String salvarCapa(Integer livroId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de imagem");
        }

        // A extensão vem do conteúdo real do arquivo, não do nome nem do Content-Type enviados pelo cliente
        String extensao = detectarExtensao(arquivo);

        // Nome gerado pelo servidor: evita sobrescrever arquivos e qualquer "../" vindo do nome original
        String nomeArquivo = "livro-" + livroId + "-" + UUID.randomUUID() + "." + extensao;

        try {
            Path pasta = Files.createDirectories(diretorioRaiz.resolve(PASTA_CAPAS));
            try (InputStream entrada = arquivo.getInputStream()) {
                Files.copy(entrada, pasta.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar a imagem", e);
        }

        return PREFIXO_URL + PASTA_CAPAS + "/" + nomeArquivo;
    }

    /**
     * Apaga o arquivo se o caminho apontar para a pasta de upload.
     * URLs externas (ex.: capa vinda do Google Books) são ignoradas.
     */
    public void removerSeLocal(String caminho) {
        if (caminho == null || !caminho.startsWith(PREFIXO_URL)) {
            return;
        }

        Path arquivo = diretorioRaiz.resolve(caminho.substring(PREFIXO_URL.length())).normalize();
        if (!arquivo.startsWith(diretorioRaiz)) {
            return;
        }

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            // Arquivo órfão não impede a operação
        }
    }

    private String detectarExtensao(MultipartFile arquivo) {
        byte[] cabecalho;
        try (InputStream entrada = arquivo.getInputStream()) {
            cabecalho = entrada.readNBytes(12);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a imagem", e);
        }

        if (comecaCom(cabecalho, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (comecaCom(cabecalho, 0x89, 'P', 'N', 'G')) {
            return "png";
        }
        if (cabecalho.length >= 12
                && new String(cabecalho, 0, 4).equals("RIFF")
                && new String(cabecalho, 8, 4).equals("WEBP")) {
            return "webp";
        }

        throw new IllegalArgumentException("Formato de imagem não suportado. Use JPG, PNG ou WEBP");
    }

    private boolean comecaCom(byte[] dados, int... assinatura) {
        if (dados.length < assinatura.length) {
            return false;
        }
        for (int i = 0; i < assinatura.length; i++) {
            if ((dados[i] & 0xFF) != assinatura[i]) {
                return false;
            }
        }
        return true;
    }
}
