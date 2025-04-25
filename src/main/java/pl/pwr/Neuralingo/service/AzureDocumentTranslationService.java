package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.pwr.Neuralingo.dto.docContent.ExtractedDocumentContent;
import pl.pwr.Neuralingo.dto.docContent.Paragraph;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AzureDocumentTranslationService {

    @Value("${azure.translator.endpoint}")
    private String endpoint;

    @Value("${azure.translator.apiKey}")
    private String apiKey;

    @Value("${azure.translator.region}")
    private String region;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tłumaczy dokument na wskazany język docelowy.
     *
     * @param src        wynik OCR (ExtractedDocumentContent)
     * @param targetLang kod ISO docelowego języka, np. "en" lub "de"
     * @return nowy ExtractedDocumentContent z przetłumaczonymi akapitami
     */
    public ExtractedDocumentContent translate(ExtractedDocumentContent src, String targetLang) {
        HttpClient client = HttpClient.newHttpClient();

        List<Paragraph> translatedParagraphs = new ArrayList<>();
        for (Paragraph p : src.paragraphs()) {
            String translated = callTranslator(client, p.content(), targetLang);
            translatedParagraphs.add(new Paragraph(translated, p.pageNumber(), p.boundingBox()));
        }

        // W tej wersji nie zmieniamy tabel, encji itp. – można dopisać analogicznie.
        String fullTranslatedText = translatedParagraphs.stream()
                .map(Paragraph::content)
                .collect(Collectors.joining("\n"));

        return new ExtractedDocumentContent(
                fullTranslatedText,
                targetLang,
                translatedParagraphs,
                src.tables(),
                src.styles(),
                src.sections(),
                src.keyValuePairs(),
                src.entities(),
                src.lines(),
                src.words());
    }

    private String callTranslator(HttpClient client, String text, String lang) {
        try {
            ArrayNode arr = mapper.createArrayNode();
            ObjectNode obj = mapper.createObjectNode();
            obj.put("Text", text);
            arr.add(obj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/translate?api-version=3.0&to=" + lang))
                    .header("Content-Type", "application/json")
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .header("Ocp-Apim-Subscription-Region", region)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(arr), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = mapper.readTree(response.body());
            return node.get(0).get("translations").get(0).get("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Błąd tłumaczenia Azure Translator", e);
        }
    }
}
