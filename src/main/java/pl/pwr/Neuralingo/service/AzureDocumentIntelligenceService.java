package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.dto.document.content.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AzureDocumentIntelligenceService {

    @Value("${azure.document.endpoint}")
    private String endpoint;

    @Value("${azure.document.apiKey}")
    private String apiKey;

    private static final String MODEL = "prebuilt-document";
    private static final String API_VERSION = "2023-07-31";

    private final ObjectMapper objectMapper;


    @Autowired                 // lub @Inject / @ConstructorBinding (jak wolisz)
    public AzureDocumentIntelligenceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Wysyła dokument do Azure i zwraca sparsowaną treść ExtractedDocumentContentDto.
     */
    public ExtractedDocumentContentDto analyzeDocument(byte[] fileBytes, String fileType) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Krok 1: submit dokumentu
            HttpRequest submit = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/formrecognizer/documentModels/" + MODEL + ":analyze?api-version=" + API_VERSION))
                    .header("Content-Type", fileType)
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<Void> submitResponse = client.send(submit, HttpResponse.BodyHandlers.discarding());

            String operationLocation = submitResponse.headers()
                    .firstValue("operation-location")
                    .orElseThrow(() -> new RuntimeException("Brak operation-location w odpowiedzi Azure."));

            // Krok 2: oczekiwanie na wynik
            JSONObject root = pollForResult(client, operationLocation);

            // Krok 3: konwersja JSON → DTO
            JSONObject analyzeResult = root.getJSONObject("analyzeResult");

            // Uwaga: zamiast ręcznego parsowania → mapujemy na AzureAnalyzeResultDto
            AzureAnalyzeResultDto azureResult = objectMapper.readValue(analyzeResult.toString(), AzureAnalyzeResultDto.class);

            // Krok 4: budowanie ExtractedDocumentContentDto
            return new ExtractedDocumentContentDto(
                    azureResult.paragraphs() != null ? azureResult.paragraphs() : new ParagraphDto[0],
                    azureResult.tables() != null ? azureResult.tables() : new TableDto[0],
                    azureResult.listItems() != null ? azureResult.listItems() : new ListItemDto[0],
                    azureResult.keyValuePairs() != null ? azureResult.keyValuePairs() : new KeyValuePairDto[0],
                    azureResult.figures() != null ? azureResult.figures() : new FigureDto[0]
            );

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas przetwarzania dokumentu przez Azure: " + e.getMessage(), e);
        }
    }

    /**
     * Polling – czeka na zakończenie analizy dokumentu.
     */
    private JSONObject pollForResult(HttpClient client, String url) throws Exception {
        for (int i = 0; i < 20; i++) { // 20 prób co 1,5 sek = max 30 sek
            HttpRequest pollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> pollResponse = client.send(pollRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject result = new JSONObject(pollResponse.body());
            String status = result.optString("status");

            switch (status) {
                case "succeeded" -> { return result; }
                case "failed" -> throw new RuntimeException("Azure zwrócił status 'failed'.");
            }

            Thread.sleep(1500); // Czekaj 1,5 sek
        }

        throw new RuntimeException("Przekroczono czas oczekiwania na wynik od Azure.");
    }
}
