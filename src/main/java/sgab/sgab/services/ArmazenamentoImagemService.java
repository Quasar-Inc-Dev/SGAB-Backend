package sgab.sgab.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import sgab.sgab.exceptions.ArmazenamentoException;

/**
 * Envia as capas para o bucket do Supabase Storage e devolve a URL pública
 * (ex.: https://<projeto>.supabase.co/storage/v1/object/public/capas/livro-3-<uuid>.jpg),
 * que é o valor salvo no imgUrl do livro. O front usa essa URL direto no <img>.
 */
@Service
public class ArmazenamentoImagemService {

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoImagemService.class);

    // Capas são exibidas no máximo com ~400 px de largura; 600 px mantém boa qualidade em telas de alta densidade
    private static final int LARGURA_MAXIMA = 600;
    private static final float QUALIDADE_JPEG = 0.8f;
    // Cada upload tem nome único (UUID), então o navegador pode guardar a imagem por 30 dias
    private static final String CACHE_CONTROL = "max-age=2592000";

    private final String bucket;
    // Início da URL pública dos arquivos do bucket; também identifica quais capas são nossas na hora de apagar
    private final String prefixoPublico;
    // null quando URL/chave do Supabase não foram configuradas
    private final RestClient storage;

    public ArmazenamentoImagemService(
            @Value("${sgab.supabase.url:}") String urlSupabase,
            @Value("${sgab.supabase.chave:}") String chave,
            @Value("${sgab.supabase.bucket-capas:capas}") String bucket) {
        String url = urlSupabase.endsWith("/") ? urlSupabase.substring(0, urlSupabase.length() - 1) : urlSupabase;
        this.bucket = bucket;
        this.prefixoPublico = url + "/storage/v1/object/public/" + bucket + "/";

        if (url.isBlank() || chave.isBlank()) {
            log.warn("Supabase Storage não configurado (sgab.supabase.url / sgab.supabase.chave); upload de capas indisponível");
            this.storage = null;
        } else {
            // A chave secreta (service_role) ignora as políticas do bucket; ela fica só no backend, nunca no front
            this.storage = RestClient.builder()
                    .baseUrl(url + "/storage/v1")
                    .defaultHeader("apikey", chave)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + chave)
                    .build();
        }
    }

    public String salvarCapa(Integer livroId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de imagem");
        }

        // A extensão vem do conteúdo real do arquivo, não do nome nem do Content-Type enviados pelo cliente
        String extensao = detectarExtensao(arquivo);

        if (storage == null) {
            throw new ArmazenamentoException("Armazenamento de capas não configurado no servidor");
        }

        byte[] conteudo;
        try {
            // JPG e PNG são reduzidos e recomprimidos como JPG; WEBP (que o ImageIO não lê) é enviado como veio
            byte[] otimizada = otimizarParaJpeg(arquivo);
            if (otimizada != null) {
                conteudo = otimizada;
                extensao = "jpg";
            } else {
                conteudo = arquivo.getBytes();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a imagem", e);
        }

        // Nome gerado pelo servidor: evita sobrescrever arquivos e qualquer "../" vindo do nome original
        String nomeArquivo = "livro-" + livroId + "-" + UUID.randomUUID() + "." + extensao;

        try {
            storage.post()
                    .uri("/object/{bucket}/{arquivo}", bucket, nomeArquivo)
                    .contentType(tipoDoArquivo(extensao))
                    .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                    .body(conteudo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Falha ao enviar a capa {} para o Supabase Storage: {}", nomeArquivo, e.getMessage());
            throw new ArmazenamentoException("Não foi possível salvar a imagem. Tente novamente.");
        }

        return prefixoPublico + nomeArquivo;
    }

    /**
     * Apaga o arquivo do bucket se a URL for de uma capa enviada por nós.
     * URLs externas (ex.: capa vinda do Google Books) são ignoradas.
     */
    public void removerSeArmazenada(String url) {
        if (storage == null || url == null || !url.startsWith(prefixoPublico)) {
            return;
        }

        String nomeArquivo = url.substring(prefixoPublico.length());
        // Só aceita nomes gerados por salvarCapa (sem subpastas nem "..")
        if (nomeArquivo.isBlank() || nomeArquivo.contains("/") || nomeArquivo.contains("..")) {
            return;
        }

        try {
            storage.delete()
                    .uri("/object/{bucket}/{arquivo}", bucket, nomeArquivo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Arquivo órfão no bucket não impede a operação
            log.warn("Não foi possível apagar a capa {} do Supabase Storage: {}", nomeArquivo, e.getMessage());
        }
    }

    private MediaType tipoDoArquivo(String extensao) {
        return switch (extensao) {
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.IMAGE_JPEG;
        };
    }

    /**
     * Reduz a imagem para no máximo LARGURA_MAXIMA px de largura e grava como JPG.
     * Devolve null se o formato não puder ser lido pelo ImageIO (ex.: WEBP).
     */
    private byte[] otimizarParaJpeg(MultipartFile arquivo) throws IOException {
        BufferedImage original;
        try (InputStream entrada = arquivo.getInputStream()) {
            original = ImageIO.read(entrada);
        }
        if (original == null) {
            return null;
        }

        double escala = Math.min(1.0, (double) LARGURA_MAXIMA / original.getWidth());
        int largura = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int altura = Math.max(1, (int) Math.round(original.getHeight() * escala));

        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE); // fundo branco para PNG com transparência
            g.fillRect(0, 0, largura, altura);
            g.drawImage(original, 0, 0, largura, altura, null);
        } finally {
            g.dispose();
        }

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam parametros = writer.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(QUALIDADE_JPEG);

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(saida)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(destino, null, null), parametros);
        } finally {
            writer.dispose();
        }
        return saida.toByteArray();
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
