package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTranslator {

    private final PDFTextReplacer textReplacer;
    private final PdfContentExtractor textExtractor;
    private final AzureDocumentTranslationService azure;

    public PdfTranslator(PDFTextReplacer textReplacer, PdfContentExtractor textExtractor, AzureDocumentTranslationService azure) {
        this.textReplacer = textReplacer;
        this.textExtractor = textExtractor;
        this.azure = azure;
    }

    public String translatePdfDocument(File pdfFile, String targetLanguage) throws IOException {
        // 1. Wyciągnięcie tekstu z PDF jako ExtractedText
        PdfContentExtractor.ExtractedText extractedText = textExtractor.extractText(pdfFile);

        // 2. Zapisz HTML tymczasowy (z data-index)
        File htmlFile = new File(pdfFile.getAbsolutePath().replace(".pdf", "_extracted.html"));
        textExtractor.saveAsHtml(extractedText, htmlFile);

        // 3. Tłumaczenie paragrafów
        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

        // 4. Podmień tekst w HTML i zapisz jako PDF
        File translatedPdfFile = textReplacer.replaceText(htmlFile, extractedText, translatedText);

        // 5. Zwróć ścieżkę do przetłumaczonego PDF
        return translatedPdfFile.getAbsolutePath();
        return "";
    }


}
