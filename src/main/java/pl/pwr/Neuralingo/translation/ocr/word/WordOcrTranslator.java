package pl.pwr.Neuralingo.translation.ocr.word;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class WordOcrTranslator {

    private final AzureDocumentTranslationService azureTranslator;
    private final WordDocumentRebuilder wordDocumentRebuilder;
    private final WordLayoutExtractor wordLayoutExtractor;
    private final AzureDocumentIntelligenceService azureIntelligence;

    public WordOcrTranslator(AzureDocumentTranslationService azureTranslator,
                             WordDocumentRebuilder wordDocumentRebuilder, WordLayoutExtractor wordLayoutExtractor,
                             AzureDocumentIntelligenceService azureIntelligence) {
        this.azureTranslator = azureTranslator;
        this.wordDocumentRebuilder = wordDocumentRebuilder;
        this.wordLayoutExtractor = wordLayoutExtractor;
        this.azureIntelligence = azureIntelligence;
    }


    public String translateOcrWordDocument(File inputFile, String targetLanguage) throws IOException {
        ExtractedText extractedText = wordLayoutExtractor.extractText(inputFile);
        TranslatedText translatedText = azureTranslator.translate(extractedText, targetLanguage);

        String originalPath = inputFile.getAbsolutePath();
        String outputPath = originalPath.replace(".docx", "") + "_translated.docx";
        File outputFile = new File(outputPath);

        wordDocumentRebuilder.rebuildDocument(inputFile, translatedText, outputFile);

        return outputFile.getAbsolutePath();
    }
}
