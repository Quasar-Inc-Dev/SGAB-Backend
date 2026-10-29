package sgab.sgab.services;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.JsonNode;
import sgab.sgab.dtos.response.LivroIsbnResponseDTO;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class LivroIsbnService {

    private final RestClient googleBooksClient;
    private final RestClient openLibraryClient;
    private final String googleApiKey;

    public LivroIsbnService(RestClient.Builder restClientBuilder,
            @Value("${google.books.api-key:}") String googleApiKey
    ) {
        this.googleApiKey = googleApiKey;
        this.googleBooksClient = restClientBuilder
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();

        this.openLibraryClient = restClientBuilder
                .baseUrl("https://openlibrary.org")
                .build();
    }

    public LivroIsbnResponseDTO consultarPorIsbn(String isbn) {
        String isbnNormalizado = normalizarIsbn(isbn);

        try {
            LivroIsbnResponseDTO resultadoGoogle = consultarGoogleBooks(isbnNormalizado);
            if (resultadoGoogle != null) {
                return resultadoGoogle;
            }
        } catch (Exception e) {
        }

        try {
            LivroIsbnResponseDTO resultadoOpenLibrary = consultarOpenLibrary(isbnNormalizado);
            if (resultadoOpenLibrary != null) {
                return resultadoOpenLibrary;
            }
        } catch (Exception e) {
        }

        String capaDireta = "https://covers.openlibrary.org/b/isbn/" + isbnNormalizado + "-L.jpg";

        return new LivroIsbnResponseDTO(
                isbnNormalizado,
                "Livro importado via ISBN (Título não localizado)",
                null,
                "Descrição indisponível nas bases consultadas.",
                "Autor Desconhecido",
                "Editora Desconhecida",
                "pt",
                null,
                null,
                "Geral",
                capaDireta
        );
    }

    private LivroIsbnResponseDTO consultarGoogleBooks(String isbn) {
        JsonNode resposta = googleBooksClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", "isbn:" + isbn)
                        .queryParam("key", googleApiKey)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        JsonNode itens = resposta == null ? null : resposta.get("items");
        if (itens == null || !itens.isArray() || itens.isEmpty()) {
            return null;
        }

        JsonNode volumeInfo = itens.get(0).path("volumeInfo");
        JsonNode imageLinks = volumeInfo.path("imageLinks");
        String thumbnail = imageLinks.has("thumbnail") ? imageLinks.get("thumbnail").asText() : null;

        return new LivroIsbnResponseDTO(
                isbn,
                textoOuNulo(volumeInfo, "title"),
                textoOuNulo(volumeInfo, "subtitle"),
                textoOuNulo(volumeInfo, "description"),
                obterAutores(volumeInfo),
                textoOuNulo(volumeInfo, "publisher"),
                textoOuNulo(volumeInfo, "language"),
                numeroOuNulo(volumeInfo, "pageCount"),
                extrairAno(volumeInfo),
                obterCategorias(volumeInfo),
                thumbnail != null ? thumbnail.replace("http://", "https://") : null
        );
    }

    private LivroIsbnResponseDTO consultarOpenLibrary(String isbn) {
        JsonNode resposta = openLibraryClient.get()
                .uri("/isbn/{isbn}.json", isbn)
                .retrieve()
                .body(JsonNode.class);

        if (resposta == null || resposta.has("error")) {
            return null;
        }

        String titulo = textoOuNulo(resposta, "title");
        Integer numPaginas = numeroOuNulo(resposta, "number_of_pages");

        String urlCapa = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";

        return new LivroIsbnResponseDTO(
                isbn,
                titulo,
                textoOuNulo(resposta, "subtitle"),
                textoOuNulo(resposta, "description"),
                "Consulte os detalhes na editora",
                null,
                "pt",
                numPaginas,
                null,
                null,
                urlCapa
        );
    }

    private String normalizarIsbn(String isbn) {
        if (isbn == null) return "";
        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }

    private String textoOuNulo(JsonNode objeto, String campo) {
        if (objeto == null) return null;
        JsonNode valor = objeto.get(campo);
        if (valor == null || valor.isNull()) return null;

        if (valor.isObject() && valor.has("value")) {
            return valor.get("value").asText();
        }

        return valor.asText();
    }

    private Integer numeroOuNulo(JsonNode objeto, String campo) {
        if (objeto == null) return null;
        JsonNode valor = objeto.get(campo);
        if (valor == null || !valor.isInt()) return null;
        return valor.asInt();
    }

    private Integer extrairAno(JsonNode volumeInfo) {
        String dataPublicacao = textoOuNulo(volumeInfo, "publishedDate");
        if (dataPublicacao == null || dataPublicacao.length() < 4) return null;
        try {
            return Integer.valueOf(dataPublicacao.substring(0, 4));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String obterAutores(JsonNode volumeInfo) {
        JsonNode autores = volumeInfo.get("authors");
        if (autores == null || !autores.isArray()) return null;
        return StreamSupport.stream(autores.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.joining("; "));
    }

    private String obterCategorias(JsonNode volumeInfo) {
        JsonNode categorias = volumeInfo.get("categories");
        if (categorias == null || !categorias.isArray()) return null;
        return StreamSupport.stream(categorias.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.joining("; "));
    }
}
