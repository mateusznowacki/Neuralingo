package pl.pwr.Neuralingo.translation.word;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WordTranslationFacade {

    public static void translateWord(String url) throws IOException {
        // 1. Pobierz plik z Azure Blob
        String localDocxPath = "downloaded.docx";
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(localDocxPath));
        }

        // 2. Wyciągnij tekst
        JSONObject extractedJson = WordTextExtractor.extractParagraphTexts(localDocxPath);
        ExtractedText extractedText = parseExtractedText(extractedJson);

        // 3. Wykonaj tłumaczenie (tu placeholder — kopiujemy tekst z prefixem "[EN]")
        List<TranslatedText.Paragraph> translatedList = new ArrayList<>();
        for (ExtractedText.Paragraph para : extractedText.paragraphs) {
            translatedList.add(new TranslatedText.Paragraph(para.index, "[EN] " + para.text));
        }
        TranslatedText translated = new TranslatedText(translatedList);

        // 4. Zapisz tymczasowy plik JSON z tłumaczeniem
        JSONObject translatedJson = new JSONObject();
        JSONArray array = new JSONArray();
        for (TranslatedText.Paragraph para : translated.paragraphs) {
            JSONObject p = new JSONObject();
            p.put("index", para.index);
            p.put("text", para.text);
            array.put(p);
        }
        translatedJson.put("paragraphs", array);
        String translatedJsonPath = "translated.json";
        try (FileWriter writer = new FileWriter(translatedJsonPath)) {
            writer.write(translatedJson.toString(2));
        }

        // 5. Podmień teksty i zapisz nowy dokument
        String translatedDocxPath = "translated_output.docx";
        WordTextReplacer.replaceParagraphs(localDocxPath, translatedJsonPath, translatedDocxPath);

        System.out.println("✅ Zakończono tłumaczenie dokumentu. Wynik zapisano jako: " + translatedDocxPath);
    }

    private static ExtractedText parseExtractedText(JSONObject json) {
        List<ExtractedText.Paragraph> result = new ArrayList<>();
        JSONArray paragraphs = json.getJSONArray("paragraphs");
        for (int i = 0; i < paragraphs.length(); i++) {
            JSONObject para = paragraphs.getJSONObject(i);
            int index = para.getInt("index");
            String text = para.getString("text");
            result.add(new ExtractedText.Paragraph(index, text));
        }
        return new ExtractedText(result);
    }
}
