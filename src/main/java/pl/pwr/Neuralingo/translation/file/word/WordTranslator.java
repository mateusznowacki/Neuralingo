package pl.pwr.Neuralingo.translation.file.word;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class WordTranslator implements DocumentTranslator {

    private final WordTextExtractor wordTextExtractor;
    private final WordTextReplacer wordTextReplacer;
    private final AzureDocumentTranslationService azure;


    public WordTranslator(WordTextExtractor wordTextExtractor, WordTextReplacer wordTextReplacer, AzureDocumentTranslationService azure) {
        this.wordTextExtractor = wordTextExtractor;
        this.wordTextReplacer = wordTextReplacer;
        this.azure = azure;
    }

    @Override
    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        ExtractedText extractedText = wordTextExtractor.extractText(inputFile);
        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

        String originalPath = inputFile.getAbsolutePath();
        String outputPath = originalPath.replace(".docx", "") + "_translated.docx";
        File outputFile = new File(outputPath);

        wordTextReplacer.replaceText(inputFile, extractedText, translatedText);

        return outputFile.getAbsolutePath();
    }
}
