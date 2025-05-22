package pl.pwr.Neuralingo.translation.file.pptx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class PptxTranslator {

    private final PptxTextExtractor extractor;
    private final PptxTextReplacer replacer;
    private final AzureDocumentTranslationService azure;

    public PptxTranslator(PptxTextExtractor extractor, PptxTextReplacer replacer, AzureDocumentTranslationService azure) {
        this.extractor = extractor;
        this.replacer = replacer;
        this.azure = azure;
    }

    public String translatePptxDocument(File file, String targetLanguage) throws IOException {
        ExtractedText extracted = extractor.extractText(file);
        TranslatedText translated = azure.translate(extracted, targetLanguage);
        File outputFile = replacer.replaceText(file, extracted, translated);
        return outputFile.getAbsolutePath();
    }
}
