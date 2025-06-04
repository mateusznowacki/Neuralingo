package pl.pwr.Neuralingo.service;

import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class AzureDocumentIntelligenceService {

    @Value("${azure.document.endpoint}")
    private String endpoint;

    @Value("${azure.document.apiKey}")
    private String apiKey;

    private static final String READ_MODEL = "prebuilt-read";
    private static final String READ_API_VERSION = "2024-11-30";

    private static final String LAYOUT_MODEL = "prebuilt-layout";
    private static final String LAYOUT_API_VERSION = "2024-11-30";


    private HttpClient client;

    @PostConstruct
    private void init() {
        client = HttpClient.newHttpClient();
    }

    public void analyzeAndSaveOnly(byte[] fileBytes, String fileType) {
        try {
            CompletableFuture<JSONObject> readFuture = sendToModel(READ_MODEL, READ_API_VERSION, fileBytes, fileType, "read");
            CompletableFuture<JSONObject> layoutFuture = sendToModel(LAYOUT_MODEL, LAYOUT_API_VERSION, fileBytes, fileType, "layout");

            readFuture.get();
            layoutFuture.get();


        } catch (Exception e) {

            throw new RuntimeException("Błąd podczas przetwarzania dokumentu: " + e.getMessage(), e);
        }
    }

    private CompletableFuture<JSONObject> sendToModel(String model, String version, byte[] bytes, String fileType, String suffix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest submit = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + "/documentintelligence/documentModels/" + model + ":analyze?api-version=" + version + "&stringIndexType=textElements"))
                        .header("Content-Type", fileType)
                        .header("Ocp-Apim-Subscription-Key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                        .build();

                HttpResponse<String> submitResponse = client.send(submit, HttpResponse.BodyHandlers.ofString());

                if (submitResponse.statusCode() >= 400) {
                    throw new RuntimeException("Błąd " + submitResponse.statusCode() + " od modelu " + model + ": " + submitResponse.body());
                }

                String opLoc = submitResponse.headers()
                        .firstValue("operation-location")
                        .orElseThrow(() -> new RuntimeException("Brak operation-location dla modelu " + model));

                JSONObject root = pollForResult(opLoc);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String filename = "response_" + suffix + "_" + timestamp + ".json";
                Path outputPath = Path.of("responses", filename);
                Files.createDirectories(outputPath.getParent());
                Files.writeString(outputPath, root.toString(2), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);


                return root;

            } catch (Exception e) {
                throw new RuntimeException("Błąd przy obsłudze modelu " + model + ": " + e.getMessage(), e);
            }
        });
    }

    private JSONObject pollForResult(String url) throws Exception {
        for (int i = 0; i < 20; i++) {
            HttpRequest pollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(pollRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject result = new JSONObject(response.body());

            String status = result.optString("status");


            switch (status) {
                case "succeeded" -> {
                    return result;
                }
                case "failed" -> throw new RuntimeException("Status 'failed': " + result.toString(2));
            }

            Thread.sleep(1500);
        }

        throw new RuntimeException("Przekroczono czas oczekiwania na wynik modelu");
    }
}
