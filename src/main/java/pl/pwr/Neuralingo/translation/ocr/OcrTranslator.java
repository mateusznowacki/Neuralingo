package pl.pwr.Neuralingo.translation.ocr;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class OcrTranslator implements DocumentTranslator {

    private final DocumentExtractor documentExtractor;

    public OcrTranslator(DocumentExtractor documentExtractor) {
        this.documentExtractor = documentExtractor;
    }

    @Override
    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        // Extract text from the document using OCR
        String html = documentExtractor.extractTextAsHtml(inputFile);


        return html;
    }
}
