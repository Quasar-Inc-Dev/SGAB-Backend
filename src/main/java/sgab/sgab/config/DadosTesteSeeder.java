package sgab.sgab.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import sgab.sgab.dtos.request.LeitorRequestDTO;
import sgab.sgab.dtos.request.LivroRequestDTO;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.UsuarioRepository;
import sgab.sgab.services.LeitorService;
import sgab.sgab.services.LivroService;

/**
 * Gera livros e leitores fictícios para testes de carga.
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
    private static final String[] AREAS = {"Tecnologia", "Gestão", "Logística", "Matemática", "Idiomas"};
    private static final String[] GENEROS = {"Técnico", "Didático", "Literatura", "Referência"};

    private final int quantidade;
    private final LivroService livroService;
    private final LeitorService leitorService;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public DadosTesteSeeder(LivroService livroService, LeitorService leitorService,
            LivroRepository livroRepository, UsuarioRepository usuarioRepository,
            @Value("${sgab.carga.quantidade:50}") int quantidade) {
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

    private int criarLivros() {
        int criados = 0;
        for (int i = 1; i <= quantidade; i++) {
            String isbn = gerarIsbn13(i);
            if (livroRepository.existsByISBN(isbn)) {
                continue;
            }
            livroService.cadastrar(new LivroRequestDTO(
                    isbn,
                    "Livro de Teste %03d".formatted(i),
                    "Volume gerado para teste de carga",
                    "Livro fictício criado automaticamente para os testes do SGAB.",
                    "Autor Teste %02d".formatted((i % 10) + 1),
                    "Editora Teste",
                    "Português",
                    100 + i * 3,
                    2000 + (i % 25),
                    GENEROS[i % GENEROS.length],
                    "teste,carga",
                    "T%03d".formatted(i),
                    "000",
                    AREAS[i % AREAS.length],
                    "ATIVO",
                    null,               // sem capa: o front mostra a inicial do título
                    1 + (i % 3)         // 1 a 3 exemplares por livro
            ));
            criados++;
        }
        return criados;
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

    /** ISBN-13 fictício (prefixo 978-65-9999) com dígito verificador válido. */
    static String gerarIsbn13(int semente) {
        String base = "978659999" + "%03d".formatted(semente % 1000);
        int soma = 0;
        for (int k = 0; k < 12; k++) {
            soma += (base.charAt(k) - '0') * (k % 2 == 0 ? 1 : 3);
        }
        int dv = (10 - soma % 10) % 10;
        return base + dv;
    }
}
