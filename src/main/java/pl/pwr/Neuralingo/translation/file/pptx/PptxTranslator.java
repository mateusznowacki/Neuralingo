package pl.pwr.Neuralingo.translation.file.pptx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class PptxTranslator implements DocumentTranslator {

    private final PptxTextExtractor extractor;
    private final PptxTextReplacer replacer;
    private final AzureDocumentTranslationService azure;

    public PptxTranslator(PptxTextExtractor extractor, PptxTextReplacer replacer, AzureDocumentTranslationService azure) {
        this.extractor = extractor;
        this.replacer = replacer;
        this.azure = azure;
    }


    @Override
    public String translateDocument(File file, String targetLanguage) throws IOException {
        ExtractedText extracted = extractor.extractText(file);
        TranslatedText translated = azure.translate(extracted, targetLanguage);
        File outputFile = replacer.replaceText(file, extracted, translated);
        return outputFile.getAbsolutePath();
    }
}
