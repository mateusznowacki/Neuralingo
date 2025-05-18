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
        System.out.println("✅ JSON zapisany dla pliku: " + pdfFile.getName());

        // 2. Tymczasowy krok: konwertuj zawartość PDF na HTML (np. dla edytora lub podglądu)
        String htmlView;
        try {
            htmlView = contentExtractor.extractLayout(pdfFile);
            System.out.println("✅ HTML wygenerowany dla: " + pdfFile.getName());
        } catch (InterruptedException e) {
            throw new IOException("PDF to HTML conversion failed", e);
        }

        // TODO: 3. Przetłumacz każdy paragraf używając np. Azure Translator API
        // TranslatedText translated = translatorService.translate(extractedText, targetLanguage);

        // TODO: 4. Wygeneruj nowy dokument PDF/Word z tłumaczeniem i layoutem

        // TODO: 5. Zapisz plik wynikowy i zwróć jego ścieżkę lub URL

        // Tymczasowo zwróć HTML do podglądu
        return htmlView;
    }


}
