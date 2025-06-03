package pl.pwr.Neuralingo.translation.file.vsdx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class VisioTranslator implements DocumentTranslator {

    private final VisioTextExtractor extractor;
    private final VisioTextReplacer replacer;
    private final AzureDocumentTranslationService azure;

    public VisioTranslator(VisioTextExtractor extractor, VisioTextReplacer replacer, AzureDocumentTranslationService azure) {
        this.extractor = extractor;
        this.replacer = replacer;
        this.azure = azure;
    }

    @Override
    public String translateDocument(File file, String targetLanguage) throws IOException {
        ExtractedText extracted = extractor.extractText(file);
        System.out.println(extracted.toString());
        TranslatedText translated = azure.translate(extracted, targetLanguage);
        System.out.println(translated.toString());
        File outputFile = replacer.replaceText(file, extracted, translated);
        return outputFile.getAbsolutePath();
    }
}



