package pl.pwr.Neuralingo.translation.file.pdf;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.translation.DocumentTranslator;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTranslator implements DocumentTranslator {

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

    @Override
    public String translateDocument(File inputFile, String targetLanguage) throws IOException {
        try {
            // 1. Konwersja PDF → HTML jako String (pdf2htmlEX)
            String htmlContent = contentExtractor.extractLayout(inputFile);

            // 2. Przebuduj HTML (scalenie wyrazów, stylizacja itp.)
            String structuredHtml = layoutParser.buildStructuredHtml(htmlContent);

            // 3. Ekstrakcja tekstu do tłumaczenia (z HTML-a jako string)
            ExtractedText extractedText = contentExtractor.extractText(structuredHtml);

            // 4. Tłumaczenie tekstu
            TranslatedText translatedText = azure.translate(extractedText, targetLanguage);

            // 5. Podmiana tekstu w HTML
            String translatedHtml = textReplacer.replaceText(structuredHtml, extractedText, translatedText);

            // 6. Generowanie końcowego PDF-a
            File translatedinputFile = new File(
                    inputFile.getParentFile(),
                    inputFile.getName().replaceFirst("(?i)\\.pdf$", "_translated.pdf")
            );

            File generatedPdf = pdfConverter.convertHtmlToPdf(
                    translatedHtml,
                    translatedinputFile
            );

            return generatedPdf.getAbsolutePath();

        } catch (InterruptedException e) {
            throw new IOException("❌ Błąd konwersji PDF → HTML lub HTML → PDF", e);
        }
    }

}

   
       
    
