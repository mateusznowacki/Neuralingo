package pl.pwr.Neuralingo.translation.ocr;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;
import pl.pwr.Neuralingo.translation.file.pdf.HtmlToPdfConverter;

import java.io.File;
import java.io.IOException;

@Component
public class OcrTranslator implements DocumentTranslator {

    private final DocumentExtractor documentExtractor;

    private final AzureDocumentTranslationService azure;

    private final TextReplacerHtml textReplacer;

    private final HtmlToPdfConverter pdfConverter;

    private final HtmltoDocxConverter htmltoDocxConverter;

    public OcrTranslator(DocumentExtractor documentExtractor, AzureDocumentTranslationService azure,
                         TextReplacerHtml textReplacer, HtmlToPdfConverter pdfConverter
            , HtmltoDocxConverter docxConverter) {
        this.documentExtractor = documentExtractor;
        this.azure = azure;
        this.textReplacer = textReplacer;
        this.pdfConverter = pdfConverter;
        this.htmltoDocxConverter = docxConverter;

    }

    @Override
    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        try {
            // Extract text from the document using OCR or native extractor
            String html = documentExtractor.extractTextAsHtml(inputFile);

            ExtractedText extractedText = documentExtractor.extractText(html);
            TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

            // Replace the original text in the HTML with the translated text
            String translatedHtml = textReplacer.replaceTextInHtml(html, extractedText, translatedText);

            String originalPath = inputFile.getAbsolutePath();

            if (originalPath.toLowerCase().endsWith(".pdf")) {
                // PDF → Puppeteer
                File translatedInputFile = new File(
                        inputFile.getParentFile(),
                        inputFile.getName().replaceFirst("(?i)\\.pdf$", "_translated.pdf")
                );
                File generatedPdf = pdfConverter.convertHtmlToPdf(translatedHtml, translatedInputFile);
                return generatedPdf.getAbsolutePath();
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + originalPath);
            }

        } catch (Exception e) {
            throw new IOException("Translation failed: " + e.getMessage(), e);
        }

    }
}
