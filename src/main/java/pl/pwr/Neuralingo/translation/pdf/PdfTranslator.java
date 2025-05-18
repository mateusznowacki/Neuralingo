package pl.pwr.Neuralingo.translation.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTranslator {

    private final PDFTextReplacer textReplacer;
    private final PdfContentExtractor contentExtractor;
    private final AzureDocumentTranslationService azure;
    private final PdfContentMerge contentMerger;

    @Autowired
    private ObjectMapper objectMapper;

    public PdfTranslator(PdfContentMerge contentMerger, PDFTextReplacer textReplacer, PdfContentExtractor contentExtractor, AzureDocumentTranslationService azure) {
        this.textReplacer = textReplacer;
        this.contentExtractor = contentExtractor;
        this.azure = azure;
        this.contentMerger = contentMerger;
    }

    public String translatePdfDocument(File pdfFile, String targetLanguage) throws IOException {
        // 1. Ekstrahuj tekst z PDF (czysty tekst)
        ExtractedText extractedText = contentExtractor.extractText(pdfFile);
        System.out.println("✅ ExtractedText zawiera: " + extractedText.paragraphs.size() + " paragrafów.");

        // 2. Konwertuj PDF na HTML (layout do podglądu)
        String htmlView;
        try {
            htmlView = contentExtractor.extractLayout(pdfFile);
        } catch (InterruptedException e) {
            throw new IOException("PDF to HTML conversion failed", e);
        }

        // 3. Przetłumacz tekst
        TranslatedText translatedText = azure.translate(extractedText, targetLanguage);
        System.out.println("✅ Przetłumaczono: " + translatedText.paragraphs.size() + " paragrafów.");

        // 4. Scal oryginał z tłumaczeniem
        ExtractedText merged = contentMerger.mergeTranslation(extractedText, translatedText);
        System.out.println("✅ Scalono oryginał z tłumaczeniem.");

        // 5. Zapisz jako JSON
        File translatedJson = new File(pdfFile.getParent(), pdfFile.getName() + ".translated.json");
        File mergedJson = new File(pdfFile.getParent(), pdfFile.getName() + ".merged.json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(translatedJson, translatedText);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(mergedJson, merged);

        System.out.println("✅ Zapisano: " + translatedJson.getName() + ", " + mergedJson.getName());

        // 6. Zwróć HTML do poglądu (np. do wyświetlenia w przeglądarce)
        return htmlView;
    }



}
