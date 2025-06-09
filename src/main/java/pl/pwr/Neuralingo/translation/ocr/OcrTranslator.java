package pl.pwr.Neuralingo.translation.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class OcrTranslator implements DocumentTranslator {

    private final DocumentExtractor documentExtractor;

    @Autowired
    private AzureDocumentTranslationService azure;

    @Autowired
    private TextReplacerHtml textReplacer;

    public OcrTranslator(DocumentExtractor documentExtractor) {
        this.documentExtractor = documentExtractor;
    }

    @Override
    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        // Extract text from the document using OCR
        String html = documentExtractor.extractTextAsHtml(inputFile);

        ExtractedText extractedText = documentExtractor.extractText(html);
        TranslatedText translatedText = azure.translate(extractedText, "en");

        // Replace the original text in the HTML with the translated text

        String translatedHtml = textReplacer.replaceTextInHtml(html, extractedText, translatedText);


        return translatedHtml;
    }
}
