package pl.pwr.Neuralingo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

private static final String MODEL = "prebuilt-document";
    private static final String API_VERSION = "2023-07-31";

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
              HttpRequest submit = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/formrecognizer/documentModels/" + MODEL + ":analyze?api-version=" + API_VERSION))
                    .header("Content-Type", fileType)
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();
            HttpResponse<Void> subResp = client.send(submit, HttpResponse.BodyHandlers.discarding());
            String opLocation = subResp.headers().firstValue("operation-location")
                    .orElseThrow(() -> new RuntimeException("Brak operation-location"));
            logger.info("Wysyłanie dokumentu do Azure...");

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

            JSONObject root = pollForResult(client, operationLocation);

            logger.info("SUROWY JSON OD AZURE:");
            logger.info(root.toString(2));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "response_read_" + timestamp + ".json";
            Path outputPath = Path.of("responses", filename);

            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, root.toString(2), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Zapisano odpowiedź JSON do pliku: {}", outputPath);

            return new ExtractedDocumentContentDto(null, null, null, null, null, null, null);
        } catch (Exception e) {
            logger.error("Błąd podczas przetwarzania dokumentu przez Azure: {}", e.getMessage(), e);
            throw new RuntimeException("Błąd podczas przetwarzania dokumentu przez Azure: " + e.getMessage(), e);
        }
    }

    private JSONObject pollForResult(HttpClient client, String url) throws Exception {
        for (int i = 0; i < 20; i++) {
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
                case "failed" ->
                        throw new RuntimeException("Azure zwrócił status 'failed'. Szczegóły: " + result.toString(2));
            }

            Thread.sleep(1500);
        }

        throw new RuntimeException("Przekroczono czas oczekiwania na wynik od Azure.");
    }
}
