package pl.pwr.Neuralingo.translation.word;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTextExtractor wordTextExtractor;
    private final WordTextReplacer wordTextReplacer;
    private final AzureDocumentTranslationService azure;

    public DocumentTranslationFacade(WordTextExtractor wordTextExtractor, WordTextReplacer wordTextReplacer, AzureDocumentTranslationService azureDocumentTranslationService) {
        this.wordTextExtractor = wordTextExtractor;
        this.wordTextReplacer = wordTextReplacer;
        this.azure = azureDocumentTranslationService;
    }

    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        ExtractedText extractedText = wordTextExtractor.extractText(inputFile);

        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);


        try {
            ObjectMapper mapper = new ObjectMapper();
            String directoryPath = "resources/temp";
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(directoryPath));

            String outputPath = directoryPath + "/"  + "_translated.json";
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), translatedText);

            System.out.println("Zapisano przetłumaczony tekst pod: " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Błąd zapisu pliku JSON", e);
        }

        // Dodaj końcówkę "_translated.docx" do oryginalnej nazwy
        String originalPath = inputFile.getAbsolutePath();
        String outputPath = originalPath.replace(".docx", "") + "_translated.docx";

        File outputFile = new File(outputPath);

        wordTextReplacer.replaceParagraphs(inputFile, extractedText, translatedText);

        return outputFile.getAbsolutePath();
    }

}
