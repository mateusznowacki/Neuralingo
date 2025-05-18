package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTranslator {

    private final PDFTextReplacer textReplacer;
    private final PdfContentExtractor contentExtractor;
    private final AzureDocumentTranslationService azure;

    public PdfTranslator(PDFTextReplacer textReplacer, PdfContentExtractor contentExtractor, AzureDocumentTranslationService azure) {
        this.textReplacer = textReplacer;
        this.contentExtractor = contentExtractor;
        this.azure = azure;
    }

    public String translatePdfDocument(File pdfFile, String targetLanguage) throws IOException {
        // 1. Ekstrahuj tekst jako obiekt DTO
        ExtractedText extractedText = contentExtractor.extractText(pdfFile);

        // 2. Tymczasowy krok: konwertuj zawartość PDF na HTML (np. dla edytora lub poglądu)
        String htmlView = null;
        try {
            htmlView = contentExtractor.extractLayout(pdfFile);
        } catch (InterruptedException e) {
            throw new IOException("PDF to HTML conversion failed", e);
        }

        // TODO: 3. Przetłumacz każdy paragraf używając np. Azure Translator API
        // TranslatedText translated = translatorService.translate(extractedText, targetLanguage);

        // TODO: 4. Wygeneruj nowy dokument PDF/Word z tłumaczeniem i layoutem

        // TODO: 5. Zapisz plik wynikowy i zwróć jego ścieżkę lub URL

        // Tymczasowo zwróć tylko HTML do podglądu
        return htmlView;
    }


}
