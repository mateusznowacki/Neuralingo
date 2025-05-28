package pl.pwr.Neuralingo.translation;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.file.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.file.pptx.PptxTranslator;
import pl.pwr.Neuralingo.translation.file.vsdx.VisioTranslator;
import pl.pwr.Neuralingo.translation.file.word.WordTranslator;
import pl.pwr.Neuralingo.translation.file.xlsx.ExcelTranslator;
import pl.pwr.Neuralingo.translation.ocr.word.WordOcrTranslator;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTranslator wordTranslator;
    private final PdfTranslator pdfTranslator;
    private final ExcelTranslator excelTranslator;
    private final PptxTranslator pptxTranslator;
    private final VisioTranslator visioTranslator;
    private final WordOcrTranslator wordOcrTranslator;

    public DocumentTranslationFacade(WordTranslator wordTranslator, PdfTranslator pdfTranslator
            , ExcelTranslator excelTranslator, PptxTranslator pptxTranslator, VisioTranslator visioTranslator
            , WordOcrTranslator wordOcrTranslator) {
        this.wordTranslator = wordTranslator;
        this.pdfTranslator = pdfTranslator;
        this.excelTranslator = excelTranslator;
        this.pptxTranslator = pptxTranslator;
        this.visioTranslator = visioTranslator;
        this.wordOcrTranslator = wordOcrTranslator;
    }

    public String translateFileDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return wordTranslator.translateWordDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pdf")) {
            return pdfTranslator.translatePdfDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pptx")) {
            return pptxTranslator.translatePptxDocument(file, targetLanguage);
        } else if (fileName.endsWith(".xlsx")) {
            return excelTranslator.translateExcelDocument(file, targetLanguage);
        } else if (fileName.endsWith(".vsdx")) {
            return visioTranslator.translateVisioDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }
    }

    public String translateOcrDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return wordOcrTranslator.translateOcrWordDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pdf")) {
            return pdfTranslator.translatePdfDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }
    }
}
