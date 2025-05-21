package pl.pwr.Neuralingo.translation;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.pptx.PptxTranslator;
import pl.pwr.Neuralingo.translation.word.WordTranslator;
import pl.pwr.Neuralingo.translation.xlsx.ExcelTranslator;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTranslator wordTranslator;
    private final PdfTranslator pdfTranslator;
    private final ExcelTranslator excelTranslator;
    private final PptxTranslator pptxTranslator;

    public DocumentTranslationFacade(WordTranslator wordTranslator, PdfTranslator pdfTranslator
            , ExcelTranslator excelTranslator, PptxTranslator pptxTranslator) {
        this.wordTranslator = wordTranslator;
        this.pdfTranslator = pdfTranslator;
        this.excelTranslator = excelTranslator;
        this.pptxTranslator = pptxTranslator;
    }

    public String translateDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return wordTranslator.translateWordDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pdf")) {
            return pdfTranslator.translatePdfDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pptx")) {
            return pptxTranslator.translatePptxDocument(file, targetLanguage);
        } else if (fileName.endsWith(".xlsx")) {
            return excelTranslator.translateExcelDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }

    }
}
