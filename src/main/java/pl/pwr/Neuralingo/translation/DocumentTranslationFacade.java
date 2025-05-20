package pl.pwr.Neuralingo.translation;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.word.WordTranslator;
import pl.pwr.Neuralingo.translation.xlsx.ExcelTranslator;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTranslator wordTranslator;
    private final PdfTranslator pdfTranslator;
    private final ExcelTranslator excelTranslator;

    public DocumentTranslationFacade(WordTranslator wordTranslator, PdfTranslator pdfTranslator
            , ExcelTranslator excelTranslator) {
        this.wordTranslator = wordTranslator;
        this.pdfTranslator = pdfTranslator;
        this.excelTranslator = excelTranslator;
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
            return excelTranslator.translateExcelDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }

    }
}
