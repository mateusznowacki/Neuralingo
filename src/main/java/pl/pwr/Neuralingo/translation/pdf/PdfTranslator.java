package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class PdfTranslator {

    private final PdfContentExtractor contentExtractor;
    private final HtmlLayoutParser layoutParser;
    private final HtmlTextReplacer textReplacer;
    private final AzureDocumentTranslationService azure;
    private final HtmlToPdfConverter pdfConverter;

    public PdfTranslator(PdfContentExtractor contentExtractor,
                         HtmlLayoutParser layoutParser,
                         HtmlTextReplacer textReplacer,
                         HtmlToPdfConverter pdfConverter,
                         AzureDocumentTranslationService azure) {
        this.contentExtractor = contentExtractor;
        this.layoutParser = layoutParser;
        this.textReplacer = textReplacer;
        this.pdfConverter = pdfConverter;
        this.azure = azure;
    }

    public String translatePdfDocument(File pdfFile, String targetLanguage) throws IOException {
        String htmlContent;

        try {
            // 1. Konwersja PDF → HTML jako String (pdf2htmlEX)
            htmlContent = contentExtractor.extractLayout(pdfFile);
        } catch (InterruptedException e) {
            throw new IOException("❌ Błąd konwersji PDF na HTML", e);
        }

        // 2. Przebuduj HTML (scalenie wyrazów, stylizacja itp.)
        String structuredHtml = layoutParser.buildStructuredHtml(htmlContent);

        // 3. Zapisz plik structured.html obok PDF
        File htmlStructuredFile = new File(
                pdfFile.getParentFile(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_structured.html")
        );
        Files.writeString(htmlStructuredFile.toPath(), structuredHtml, StandardCharsets.UTF_8);
        System.out.println("✅ [1] Zapisano structured HTML: " + htmlStructuredFile.getName());

        // 4. Ekstrakcja tekstu do tłumaczenia
        ExtractedText extractedText = contentExtractor.extractText(structuredHtml);

        // 5. Tłumaczenie tekstu
        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

        // 6. Podmiana tekstu w HTML
        String translatedHtml = textReplacer.replaceText(structuredHtml, extractedText, translatedText);

        // 7. Zapisz przetłumaczony HTML
        File translatedHtmlFile = new File(
                pdfFile.getParentFile(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_translated.html")
        );
        Files.writeString(translatedHtmlFile.toPath(), translatedHtml, StandardCharsets.UTF_8);
        System.out.println("✅ [2] Zapisano translated HTML: " + translatedHtmlFile.getName());

        // 8. Konwersja HTML → PDF
        File translatedPdfFile = new File(
                pdfFile.getParentFile(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_translated.pdf")
        );

        try {
            File generatedPdf = pdfConverter.convertHtmlToPdf(translatedHtmlFile, translatedPdfFile); // zakładamy, że metoda przyjmuje input + output
            System.out.println("✅ [3] Wygenerowano translated PDF: " + generatedPdf.getName());
            return generatedPdf.getAbsolutePath();

        } catch (IOException | InterruptedException e) {
            throw new IOException("❌ Błąd konwersji HTML na PDF", e);
        }


    }

}
