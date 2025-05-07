package pl.pwr.Neuralingo.service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.dto.document.DocumentEntityDto;
import pl.pwr.Neuralingo.dto.document.content.*;

@Service
public class AzureDocumentIntelligenceService {

    @Value("${azure.document.endpoint}")
    private String endpoint;

    @Value("${azure.document.apiKey}")
    private String apiKey;

    private static final String MODEL = "prebuilt-layout";
    private static final String API_VERSION = "2024-11-30";

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AzureDocumentIntelligenceService.class);

    private HttpClient client;

    @Autowired
    public AzureDocumentIntelligenceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        client = HttpClient.newHttpClient();
    }

    public ExtractedDocumentContentDto analyzeDocument(byte[] fileBytes, String fileType) {
        try {
            // Krok 1: submit dokumentu
            HttpRequest submit = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/documentintelligence/documentModels/" + MODEL + ":analyze?api-version=" + API_VERSION))
                    .header("Content-Type", fileType)
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<String> submitResponse = client.send(submit, HttpResponse.BodyHandlers.ofString());

            logger.info("Kod odpowiedzi submit: {}", submitResponse.statusCode());
            submitResponse.headers().map().forEach((k, v) -> logger.info("Header: {} = {}", k, v));
            logger.info("Body odpowiedzi submit: {}", submitResponse.body());

            if (submitResponse.statusCode() >= 400) {
                throw new RuntimeException("Azure zwrócił błąd HTTP " + submitResponse.statusCode() + ": " + submitResponse.body());
            }

            String operationLocation = submitResponse.headers()
                    .firstValue("operation-location")
                    .orElseThrow(() -> new RuntimeException("Brak operation-location w odpowiedzi Azure."));

            // Krok 2: oczekiwanie na wynik
            JSONObject root = pollForResult(client, operationLocation);

            logger.info("SUROWY JSON OD AZURE:");
            logger.info(root.toString(2)); // pretty-print JSON

            // Krok 3: konwersja JSON → DTO
            JSONObject analyzeResult = root.getJSONObject("analyzeResult");

            AzureAnalyzeResultDto azureResult = objectMapper.readValue(analyzeResult.toString(), AzureAnalyzeResultDto.class);

            // Krok 4: budowanie ExtractedDocumentContentDto
            return new ExtractedDocumentContentDto(
                    azureResult.paragraphs() != null ? azureResult.paragraphs() : new ParagraphDto[0],
                    azureResult.tables() != null ? azureResult.tables() : new TableDto[0],
                    azureResult.listItems() != null ? azureResult.listItems() : new ListItemDto[0],
                    azureResult.keyValuePairs() != null ? azureResult.keyValuePairs() : new KeyValuePairDto[0],
                    azureResult.figures() != null ? azureResult.figures() : new FigureDto[0],
                    azureResult.entities() != null ? azureResult.entities() : new DocumentEntityDto[0],
                    azureResult.relationships() != null ? azureResult.relationships() : new DocumentEntityRelationDto[0]
            );

        } catch (Exception e) {
            logger.error("Błąd podczas przetwarzania dokumentu przez Azure: {}", e.getMessage(), e);
            throw new RuntimeException("Błąd podczas przetwarzania dokumentu przez Azure: " + e.getMessage(), e);
        }
    }

    private JSONObject pollForResult(HttpClient client, String url) throws Exception {
        for (int i = 0; i < 20; i++) { // max 30 sekund
            HttpRequest pollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> pollResponse = client.send(pollRequest, HttpResponse.BodyHandlers.ofString());

            logger.info("Poll response [{}]: {}", i + 1, pollResponse.statusCode());
            JSONObject result = new JSONObject(pollResponse.body());

            String status = result.optString("status");
            logger.info("Status analizy: {}", status);

            switch (status) {
                case "succeeded" -> {
                    return result;
                }
                case "failed" -> throw new RuntimeException("Azure zwrócił status 'failed'. Szczegóły: " + result.toString(2));
            }

            Thread.sleep(1500);
        }

        throw new RuntimeException("Przekroczono czas oczekiwania na wynik od Azure.");
    }
}
