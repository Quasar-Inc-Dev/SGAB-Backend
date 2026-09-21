package sgab.sgab.Services;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;


import sgab.sgab.dtos.response.LivroIsbnResponseDTO;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class LivroIsbnService {

    private final RestClient restClient;
    private final String apiKey;

    public LivroIsbnService(RestClient.Builder restClientBuilder,
        @Value("${google.books.api-key:}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = restClientBuilder
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();
    }

    public LivroIsbnResponseDTO consultarPorIsbn(String isbn) {
        String isbnNormalizado = normalizarIsbn(isbn);

        JsonNode resposta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", "isbn:" + isbnNormalizado)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        JsonNode itens = resposta == null ? null : resposta.get("items");

        if (itens == null || !itens.isArray() || itens.isEmpty()) {
            throw new LivroNaoEncontradoException(
                    "Nenhum livro encontrado para o ISBN informado"
            );
        }

        JsonNode volumeInfo = itens.get(0).path("volumeInfo");

        return new LivroIsbnResponseDTO(
                isbnNormalizado,
                textoOuNulo(volumeInfo, "title"),
                textoOuNulo(volumeInfo, "subtitle"),
                textoOuNulo(volumeInfo, "description"),
                obterAutores(volumeInfo),
                textoOuNulo(volumeInfo, "publisher"),
                textoOuNulo(volumeInfo, "language"),
                numeroOuNulo(volumeInfo, "pageCount"),
                extrairAno(volumeInfo),
                obterCategorias(volumeInfo)
        );
    }

    private String normalizarIsbn(String isbn) {
        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }

    private String textoOuNulo(JsonNode objeto, String campo) {
        JsonNode valor = objeto.get(campo);

        if (valor == null || valor.isNull()) {
            return null;
        }

        return valor.asText();
    }

    private Integer numeroOuNulo(JsonNode objeto, String campo) {
        JsonNode valor = objeto.get(campo);

        if (valor == null || !valor.isInt()) {
            return null;
        }

        return valor.asInt();
    }

    private Integer extrairAno(JsonNode volumeInfo) {
        String dataPublicacao = textoOuNulo(volumeInfo, "publishedDate");

        if (dataPublicacao == null || dataPublicacao.length() < 4) {
            return null;
        }

        try {
            return Integer.valueOf(dataPublicacao.substring(0, 4));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String obterAutores(JsonNode volumeInfo) {
        JsonNode autores = volumeInfo.get("authors");

        if (autores == null || !autores.isArray()) {
            return null;
        }

        return StreamSupport.stream(autores.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.joining("; "));
    }

    private String obterCategorias(JsonNode volumeInfo) {
        JsonNode categorias = volumeInfo.get("categories");

        if (categorias == null || !categorias.isArray()) {
            return null;
        }

        return StreamSupport.stream(categorias.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.joining("; "));
    }
}
