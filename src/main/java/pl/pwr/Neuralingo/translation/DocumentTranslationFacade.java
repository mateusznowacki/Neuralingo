package pl.pwr.Neuralingo.translation;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.word.WordTranslator;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTranslator wordTranslator;
    private final PdfTranslator pdfTranslator;

    public DocumentTranslationFacade(WordTranslator wordTranslator, PdfTranslator pdfTranslator) {
        this.wordTranslator = wordTranslator;
        this.pdfTranslator = pdfTranslator;
    }

    public String translateDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return wordTranslator.translateWordDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pdf")) {
            return pdfTranslator.translatePdfDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pptx")) {
            // TODO: analogicznie translatePptx(...)
            throw new UnsupportedOperationException("Tłumaczenie PPTX nie jest jeszcze wspierane");
        } else if (fileName.endsWith(".xlsx")) {
            // TODO: translateExcel(...)
            throw new UnsupportedOperationException("Tłumaczenie XLSX nie jest jeszcze wspierane");
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }

    }
}
