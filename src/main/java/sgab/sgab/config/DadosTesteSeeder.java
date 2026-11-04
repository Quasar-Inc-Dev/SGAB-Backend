package sgab.sgab.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.request.LivroRequestDTO;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.UsuarioRepository;
import sgab.sgab.services.LeitorService;
import sgab.sgab.services.LivroService;

/**
 * Gera livros reais (Google Books API, com a Open Library como alternativa) e leitores fictícios
 * para testes de carga.
 *
 * Só roda com o profile "carga". Passa pelos services (e não por SQL) para que
 * a senha seja gravada com BCrypt, o CPF criptografado e os exemplares criados.
 * Pode ser executado várias vezes: o que já existe é ignorado.
 *
 * Exemplo: SPRING_PROFILES_ACTIVE=carga SGAB_EMAIL_HABILITADO=false
 */
@Component
@Profile("carga")
public class DadosTesteSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosTesteSeeder.class);

    public static final String SENHA_PADRAO = "Teste@123";

    // Área do acervo -> {busca na Google Books API, busca na Open Library}
    private static final Map<String, String[]> BUSCAS = new LinkedHashMap<>();
    // Área do acervo -> classe principal da CDD (Dewey)
    private static final Map<String, String> DEWEY = Map.of(
            "Tecnologia", "005", "Gestão", "658", "Matemática", "510", "Literatura", "869");
    static {
        BUSCAS.put("Tecnologia", new String[] {"subject:computers", "programação"});
        BUSCAS.put("Gestão", new String[] {"subject:business", "administração"});
        BUSCAS.put("Matemática", new String[] {"subject:mathematics", "matemática"});
        BUSCAS.put("Literatura", new String[] {"subject:fiction", "romance"});
    }

    /** Dados mínimos de um livro encontrado em uma das APIs. */
    private record Candidato(String isbn, String titulo, String subtitulo, String descricao, String autor,
            String editora, int paginas, Integer ano, String categorias, String capa) {}

    private final int quantidade;
    private final LivroService livroService;
    private final LeitorService leitorService;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestClient googleBooks;
    private final RestClient openLibrary;
    private final String googleApiKey;

    public DadosTesteSeeder(LivroService livroService, LeitorService leitorService,
            LivroRepository livroRepository, UsuarioRepository usuarioRepository,
            RestClient.Builder restClientBuilder,
            @Value("${google.books.api-key:}") String googleApiKey,
            @Value("${sgab.carga.quantidade:50}") int quantidade) {
        this.googleBooks = restClientBuilder.clone().baseUrl("https://www.googleapis.com/books/v1").build();
        this.openLibrary = restClientBuilder.clone().baseUrl("https://openlibrary.org").build();
        this.googleApiKey = googleApiKey;
        this.livroService = livroService;
        this.leitorService = leitorService;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.quantidade = quantidade;
    }

    @Override
    public void run(String... args) {
        int livros = criarLivros();
        int leitores = criarLeitores();
        log.info("Dados de teste: {} livro(s) e {} leitor(es) criados (senha dos leitores: {})",
                livros, leitores, SENHA_PADRAO);
    }

    /**
     * Busca livros reais em português, divididos entre as áreas de BUSCAS. Tenta primeiro a
     * Google Books API e completa com a Open Library. Só entram livros com ISBN-13, título,
     * autor, editora e número de páginas, que são os campos obrigatórios do cadastro.
     */
    private int criarLivros() {
        int porArea = (int) Math.ceil((double) quantidade / BUSCAS.size());
        int criados = 0;

        for (Map.Entry<String, String[]> busca : BUSCAS.entrySet()) {
            String area = busca.getKey();
            List<Candidato> candidatos = new ArrayList<>(buscarGoogle(area, busca.getValue()[0]));
            candidatos.addAll(buscarOpenLibrary(area, busca.getValue()[1]));

            int criadosNaArea = 0;
            for (Candidato c : candidatos) {
                if (criadosNaArea >= porArea || criados >= quantidade) {
                    break;
                }
                if (livroRepository.existsByISBN(c.isbn())) {
                    continue;
                }
                try {
                    livroService.cadastrar(new LivroRequestDTO(
                            c.isbn(),
                            limitar(c.titulo(), 50),
                            limitar(c.subtitulo(), 100),
                            limitar(c.descricao(), 200),
                            limitar(c.autor(), 100),
                            limitar(c.editora(), 100),
                            "Português",
                            c.paginas(),
                            c.ano(),
                            limitar(c.categorias(), 100),
                            "teste,carga",
                            pha(c.autor()),         // aproximação: não usa a tabela Cutter-Sanborn
                            DEWEY.get(area),
                            area,
                            "ATIVO",
                            c.capa(),
                            1 + (criados % 3)       // 1 a 3 exemplares por livro
                    ));
                    criados++;
                    criadosNaArea++;
                } catch (RuntimeException e) {
                    log.warn("Livro {} ignorado: {}", c.isbn(), e.getMessage());
                }
            }
            log.info("Área {}: {} candidato(s) encontrados, {} livro(s) cadastrados", area, candidatos.size(), criadosNaArea);
        }
        return criados;
    }

    private List<Candidato> buscarGoogle(String area, String consulta) {
        List<Candidato> lista = new ArrayList<>();
        try {
            JsonNode resposta = googleBooks.get()
                    .uri(uri -> uri.path("/volumes")
                            .queryParam("q", consulta)
                            .queryParam("langRestrict", "pt")
                            .queryParam("printType", "books")
                            .queryParam("maxResults", 40)
                            .queryParamIfPresent("key", googleApiKey.isBlank() ? Optional.empty() : Optional.of(googleApiKey))
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode itens = resposta == null ? null : resposta.get("items");
            if (itens == null || !itens.isArray()) {
                log.info("Google Books não retornou livros para {}", area);
                return lista;
            }
            for (JsonNode item : itens) {
                JsonNode info = item.path("volumeInfo");
                String isbn = isbnGoogle(info);
                String titulo = texto(info, "title");
                String autor = juntar(info.get("authors"));
                String editora = texto(info, "publisher");
                int paginas = info.path("pageCount").asInt(0);
                if (isbn != null && titulo != null && autor != null && editora != null && paginas > 0) {
                    lista.add(new Candidato(isbn, titulo, texto(info, "subtitle"), texto(info, "description"),
                            autor, editora, paginas, ano(texto(info, "publishedDate")),
                            juntar(info.get("categories")), capaGoogle(info)));
                }
            }
        } catch (Exception e) {
            // 429 = limite diário sem chave; 400 = chave inválida. Configure google.books.api-key.
            log.warn("Google Books indisponível para {}: {}", area, e.getMessage());
        }
        return lista;
    }

    private List<Candidato> buscarOpenLibrary(String area, String termo) {
        List<Candidato> lista = new ArrayList<>();
        try {
            JsonNode resposta = openLibrary.get()
                    .uri(uri -> uri.path("/search.json")
                            .queryParam("q", termo + " language:por")
                            .queryParam("fields", "title,subtitle,author_name,publisher,isbn,number_of_pages_median,first_publish_year,subject")
                            .queryParam("limit", 100)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode docs = resposta == null ? null : resposta.get("docs");
            if (docs == null || !docs.isArray()) {
                return lista;
            }
            for (JsonNode doc : docs) {
                String isbn = isbnOpenLibrary(doc.get("isbn"));
                String titulo = texto(doc, "title");
                String autor = juntar(doc.get("author_name"));
                String editora = primeiro(doc.get("publisher"));
                int paginas = doc.path("number_of_pages_median").asInt(0);
                if (isbn != null && titulo != null && autor != null && editora != null && paginas > 0) {
                    lista.add(new Candidato(isbn, titulo, texto(doc, "subtitle"), null, autor, editora, paginas,
                            doc.path("first_publish_year").isNumber() ? doc.path("first_publish_year").asInt() : null,
                            primeiro(doc.get("subject")),
                            "https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg"));
                }
            }
        } catch (Exception e) {
            log.warn("Open Library indisponível para {}: {}", area, e.getMessage());
        }
        return lista;
    }

    /** ISBN-13 do volume; se só houver ISBN-10, converte para 13. */
    private static String isbnGoogle(JsonNode info) {
        String isbn10 = null;
        for (JsonNode id : info.path("industryIdentifiers")) {
            String tipo = id.path("type").asText();
            String valor = id.path("identifier").asText();
            if ("ISBN_13".equals(tipo)) {
                return valor;
            }
            if ("ISBN_10".equals(tipo)) {
                isbn10 = valor;
            }
        }
        return isbn10 == null ? null : isbn10Para13(isbn10);
    }

    /** Prefere ISBN-13 de editoras brasileiras (978-85 e 978-65). */
    private static String isbnOpenLibrary(JsonNode isbns) {
        if (isbns == null || !isbns.isArray()) return null;
        String qualquer13 = null;
        for (JsonNode n : isbns) {
            String v = n.asText().replaceAll("[^0-9]", "");
            if (v.length() == 13 && (v.startsWith("97885") || v.startsWith("97865"))) return v;
            if (v.length() == 13 && qualquer13 == null && v.startsWith("978")) qualquer13 = v;
        }
        if (qualquer13 != null) return qualquer13;
        for (JsonNode n : isbns) {
            String v = n.asText().replaceAll("[^0-9Xx]", "");
            if (v.length() == 10) return isbn10Para13(v);
        }
        return null;
    }

    private static String isbn10Para13(String isbn10) {
        String base = "978" + isbn10.replaceAll("[^0-9Xx]", "").substring(0, 9);
        int soma = 0;
        for (int k = 0; k < 12; k++) {
            soma += (base.charAt(k) - '0') * (k % 2 == 0 ? 1 : 3);
        }
        return base + (10 - soma % 10) % 10;
    }

    private static String primeiro(JsonNode lista) {
        if (lista == null || !lista.isArray() || lista.isEmpty()) return null;
        String v = lista.get(0).asText();
        return v.isBlank() ? null : v;
    }

    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        return valor == null || valor.isNull() || valor.asText().isBlank() ? null : valor.asText();
    }

    private static String juntar(JsonNode lista) {
        if (lista == null || !lista.isArray() || lista.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode valor : lista) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(valor.asText());
        }
        return sb.toString();
    }



    private static Integer ano(String data) {
        if (data == null || data.length() < 4) return null;
        try {
            return Integer.parseInt(data.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String capaGoogle(JsonNode info) {
        String url = texto(info.path("imageLinks"), "thumbnail");
        if (url == null) return null;
        url = url.replace("http://", "https://").replace("&edge=curl", "");
        return url.length() <= 200 ? url : null;
    }

    /** Primeira letra do sobrenome do primeiro autor + "000" (placeholder de número de chamada). */
    private static String pha(String autores) {
        String primeiro = autores.split(";")[0].trim();
        String[] partes = primeiro.split("\\s+");
        String sobrenome = partes[partes.length - 1];
        char inicial = Character.toUpperCase(sobrenome.isEmpty() ? 'X' : sobrenome.charAt(0));
        return inicial + "000";
    }

    private static String limitar(String valor, int maximo) {
        if (valor == null) return null;
        return valor.length() <= maximo ? valor : valor.substring(0, maximo - 1).trim() + "…";
    }

    private int criarLeitores() {
        int criados = 0;
        for (int i = 1; i <= quantidade; i++) {
            String email = "sgab.teste%03d@aluno.cps.sp.gov.br".formatted(i);
            if (usuarioRepository.existsByEmail(email)) {
                continue;
            }
            leitorService.cadastrar(new LeitorRequestDTO(
                    gerarCpf(i),
                    "Leitor Teste %03d".formatted(i),
                    email,
                    SENHA_PADRAO,
                    i % 2 == 0 ? "Feminino" : "Masculino",
                    LocalDate.of(1990 + (i % 15), 1 + (i % 12), 1 + (i % 28)),
                    i % 5 == 0 ? "PROFESSOR" : "LEITOR"
            ));
            criados++;
        }
        return criados;
    }

    /** CPF fictício com dígitos verificadores válidos. */
    static String gerarCpf(int semente) {
        String base = "%09d".formatted(123_000_000 + semente * 7_919);
        int[] d = base.chars().map(c -> c - '0').toArray();

        int soma = 0;
        for (int k = 0; k < 9; k++) soma += d[k] * (10 - k);
        int dv1 = 11 - soma % 11;
        if (dv1 >= 10) dv1 = 0;

        soma = 0;
        for (int k = 0; k < 9; k++) soma += d[k] * (11 - k);
        soma += dv1 * 2;
        int dv2 = 11 - soma % 11;
        if (dv2 >= 10) dv2 = 0;

        return base + dv1 + dv2;
    }
}
