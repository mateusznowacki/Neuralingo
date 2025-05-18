package pl.pwr.Neuralingo.translation.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class PdfTranslator {

    private final PdfContentExtractor contentExtractor;
    private final HtmlLayoutParser layoutParser;
    private final PdfContentMerge contentMerger;
    private final AzureDocumentTranslationService azure;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PdfTranslator(PdfContentExtractor contentExtractor,
                         HtmlLayoutParser layoutParser,
                         PdfContentMerge contentMerger,
                         AzureDocumentTranslationService azure) {
        this.contentExtractor = contentExtractor;
        this.layoutParser = layoutParser;
        this.contentMerger = contentMerger;
        this.azure = azure;
    }

    public String translatePdfDocument(File pdfFile, String lang) throws IOException {

        String htmlContent;
        try {
            // 1. Konwersja PDF → HTML jako String (pdf2htmlEX)
            htmlContent = contentExtractor.extractLayout(pdfFile);
            System.out.println("✅ [1] Konwersja PDF zakończona.");
        } catch (InterruptedException e) {
            throw new IOException("❌ Błąd konwersji PDF na HTML", e);
        }

// 2. Budowanie nowego HTML z czystą strukturą (<p>, <table>) na podstawie oryginalnego HTML
        String structuredHtml = layoutParser.buildStructuredHtml(htmlContent);

// 3. Zapisz nowy HTML do pliku obok PDF (np. plik_structured.html)
        File htmlOutput = new File(
                pdfFile.getParent(),
                pdfFile.getName().replaceFirst("(?i)\\.pdf$", "_structured.html")
        );
        Files.writeString(htmlOutput.toPath(), structuredHtml);
        System.out.println("✅ [2] Zapisano nowy HTML: " + htmlOutput.getName());


// 5. Zapisz ExtractedText jako JSON (do dalszego użycia, np. tłumaczenia)
//        File parsedJson = new File(pdfFile.getParent(), pdfFile.getName().replaceFirst("(?i)\\.pdf$", ".html_text.json"));
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(parsedJson, extractedText);
//        System.out.println("✅ [4] Zapisano tekst z HTML jako JSON: " + parsedJson.getName());

//
//        // 4. Przetłumacz tekst
//        TranslatedText translated = azure.translate(parsedFromHtml, lang);
//        System.out.println("✅ [4] Przetłumaczono tekst.");
//
//        // 5. Scal oryginał z tłumaczeniem
//        ExtractedText merged = contentMerger.mergeTranslation(parsedFromHtml, translated);
//        System.out.println("✅ [5] Scalono oryginał z tłumaczeniem.");
//
//        // 6. Zapisz jako JSON
//        File translatedJson = new File(pdfFile.getParent(), pdfFile.getName().replaceFirst("(?i)\\.pdf$", ".translated.json"));
//        File mergedJson = new File(pdfFile.getParent(), pdfFile.getName().replaceFirst("(?i)\\.pdf$", ".merged.json"));
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(translatedJson, translated);
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(mergedJson, merged);
//        System.out.println("✅ [6] Zapisano pliki: " + translatedJson.getName() + ", " + mergedJson.getName());

        return htmlContent;
    }
}
