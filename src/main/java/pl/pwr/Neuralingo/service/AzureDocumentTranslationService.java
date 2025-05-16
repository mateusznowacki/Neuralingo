package pl.pwr.Neuralingo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

@Service
public class AzureDocumentTranslationService {

    @Value("${azure.translator.endpoint}")
    private String translatorEndpoint;

    @Value("${azure.translator.apiKey}")
    private String translatorApiKey;

    @Value("${azure.translator.region}")
    private String translatorRegion;

    public TranslatedText translate(ExtractedText extractedText, String targetLanguage) {
        List<TranslatedText.Paragraph> translatedParagraphs = new ArrayList<>();

        for (ExtractedText.Paragraph para : extractedText.paragraphs) {
            String translated = translateSingleText(para.text, targetLanguage);
            translatedParagraphs.add(new TranslatedText.Paragraph(para.index, translated));
        }

        return new TranslatedText(translatedParagraphs);
    }

    private String translateSingleText(String text, String targetLanguage) {
        try {
            URL url = new URL(translatorEndpoint + "/translate?api-version=3.0&to=" + targetLanguage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", translatorApiKey);
            connection.setRequestProperty("Ocp-Apim-Subscription-Region", translatorRegion);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONArray requestBody = new JSONArray();
            JSONObject textObj = new JSONObject();
            textObj.put("Text", text);
            requestBody.put(textObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            JSONArray jsonArray = new JSONArray(response.toString());
            return jsonArray.getJSONObject(0)
                            .getJSONArray("translations")
                            .getJSONObject(0)
                            .getString("text");

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas tłumaczenia tekstu przez Azure Translator", e);
        }
    }
}
