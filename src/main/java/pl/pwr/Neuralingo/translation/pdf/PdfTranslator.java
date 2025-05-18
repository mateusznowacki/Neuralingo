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

    public PdfTranslator(PdfContentExtractor contentExtractor,
                         HtmlLayoutParser layoutParser,
                         HtmlTextReplacer textReplacer,
                         AzureDocumentTranslationService azure) {
        this.contentExtractor = contentExtractor;
        this.layoutParser = layoutParser;
        this.textReplacer = textReplacer;
        this.azure = azure;
    }

    public String translatePdfDocument(File pdfFile, String targetLanguage) throws IOException {

        String htmlContent;
        try {
            // 1. Konwersja PDF → HTML jako String (pdf2htmlEX)
            htmlContent = contentExtractor.extractLayout(pdfFile);
        } catch (InterruptedException e) {
            throw new IOException("Błąd konwersji PDF na HTML", e);
        }

        String structuredHtml = layoutParser.buildStructuredHtml(htmlContent);

// 3. Zapisz nowy HTML do pliku obok PDF (np. plik_structured.html)
        File htmlOutput = new File(
                pdfFile.getParent(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_structured.html")
        );
        Files.writeString(htmlOutput.toPath(), structuredHtml);
        System.out.println("✅ [2] Zapisano nowy HTML: " + htmlOutput.getName());

        ExtractedText extractedText = contentExtractor.extractText(structuredHtml);

        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

        String translatedHtml = textReplacer.replaceText(structuredHtml, extractedText, translatedText);


// wygeneruj nazwę pliku na podstawie PDF
        File translatedHtmlFile = new File(
                pdfFile.getParentFile(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_translated.html")
        );

// zapisz jako HTML
        Files.writeString(translatedHtmlFile.toPath(), translatedHtml, StandardCharsets.UTF_8);


        return "";

    }
}
