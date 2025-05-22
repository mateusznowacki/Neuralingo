package pl.pwr.Neuralingo.translation.file.xlsx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class ExcelTranslator {

    private final ExcelTextExtractor extractor;
    private final ExcelTextReplacer replacer;
    private final AzureDocumentTranslationService azure;

    public ExcelTranslator(ExcelTextExtractor extractor, ExcelTextReplacer replacer, AzureDocumentTranslationService azure) {
        this.extractor = extractor;
        this.replacer = replacer;
        this.azure = azure;
    }

    public String translateExcelDocument(File inputFile, String targetLanguage) throws IOException {
        ExtractedText extractedText = extractor.extractText(inputFile);
        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);
        File outputFile = replacer.replaceText(inputFile, extractedText, translatedText);
        return outputFile.getAbsolutePath();
    }

}

