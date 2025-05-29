package pl.pwr.Neuralingo.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class AzureDocumentTranslationService {

    @Value("${azure.translator.endpoint}")
    private String translatorEndpoint;

    @Value("${azure.translator.apiKey}")
    private String translatorApiKey;

    @Value("${azure.translator.region}")
    private String translatorRegion;

    public TranslatedText translate(ExtractedText extractedText, String targetLanguage) {
        List<Paragraph> translatedParagraphs = new ArrayList<>();
        List<Integer> toTranslateIndexes = new ArrayList<>();
        List<String> toTranslateTexts = new ArrayList<>();

        for (Paragraph para : extractedText.getParagraphs()) {
            if (shouldTranslate(para.getText())) {
                toTranslateIndexes.add(para.getIndex());
                toTranslateTexts.add(para.getText());
            } else {
                translatedParagraphs.add(new Paragraph(para.getIndex(), para.getText()));
            }
        }

        List<String> translatedTexts = translateBatch(toTranslateTexts, targetLanguage);

        for (int i = 0; i < toTranslateIndexes.size(); i++) {
            translatedParagraphs.add(new Paragraph(toTranslateIndexes.get(i), translatedTexts.get(i)));
        }

        translatedParagraphs.sort(Comparator.comparingInt(Paragraph::getIndex));
        return new TranslatedText(translatedParagraphs);
    }

    private boolean shouldTranslate(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        return text.trim().matches(".*[\\p{L}].*");
    }

    List<String> translateBatch(List<String> texts, String targetLanguage) {
        if (texts.isEmpty()) return Collections.emptyList();

        try {
            URL url = new URL(translatorEndpoint + "/translate?api-version=3.0&to=" + targetLanguage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", translatorApiKey);
            connection.setRequestProperty("Ocp-Apim-Subscription-Region", translatorRegion);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONArray requestBody = new JSONArray();
            for (String text : texts) {
                JSONObject textObj = new JSONObject();
                textObj.put("Text", text);
                requestBody.put(textObj);
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
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
            List<String> translated = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String translatedText = jsonArray.getJSONObject(i)
                        .getJSONArray("translations")
                        .getJSONObject(0)
                        .getString("text");
                translated.add(translatedText);
            }

            return translated;

        } catch (Exception e) {
            throw new RuntimeException("Error while translating text via Azure Translator API", e);
        }
    }
}
