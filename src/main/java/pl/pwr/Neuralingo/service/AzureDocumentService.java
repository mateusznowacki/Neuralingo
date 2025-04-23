package pl.pwr.Neuralingo.service;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

@Service
public class AzureDocumentService {

    @Value("${azure.document.endpoint}")
    private String documentEndpoint;

    @Value("${azure.document.key}")
    private String documentKey;

    @Value("${azure.translator.key}")
    private String translatorKey;

    @Value("${azure.translator.endpoint}")
    private String translatorEndpoint;

    private final OriginalDocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public AzureDocumentService(OriginalDocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    public String analyzeAndTranslate(String documentId, String targetLanguage) throws IOException, InterruptedException {
        OriginalDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        File file = new File(doc.getStoragePath());
        BinaryData binaryData = BinaryData.fromFile(Paths.get(file.getAbsolutePath()));

        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(documentKey))
                .endpoint(documentEndpoint)
                .buildClient();

        AnalyzeResult result = client.beginAnalyzeDocument("prebuilt-document", binaryData).getFinalResult();

        StringBuilder text = new StringBuilder();
        for (DocumentPage page : result.getPages()) {
            for (DocumentLine line : page.getLines()) {
                text.append(line.getContent()).append("\n");
            }
        }

        return translateText(text.toString(), targetLanguage);
    }

    private String translateText(String text, String to) throws IOException, InterruptedException {
        String url = translatorEndpoint + "/translate?api-version=3.0&to=" + to;

        String requestBody = "[{\"Text\":\"" + text.replace("\"", "\\\"") + "\"}]";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Ocp-Apim-Subscription-Key", translatorKey)
                .header("Ocp-Apim-Subscription-Region", "westeurope")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = objectMapper.readTree(response.body());
        return json.get(0).get("translations").get(0).get("text").asText();
    }
}
