package pl.pwr.Neuralingo.translation;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.translation.file.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.file.pptx.PptxTranslator;
import pl.pwr.Neuralingo.translation.file.vsdx.VisioTranslator;
import pl.pwr.Neuralingo.translation.file.word.WordTranslator;
import pl.pwr.Neuralingo.translation.file.xlsx.ExcelTranslator;
import pl.pwr.Neuralingo.translation.ocr.OcrTranslator;

import java.io.File;
import java.io.IOException;


@Component
public class DocumentTranslationFacade {

    private final WordTranslator wordTranslator;
    private final PdfTranslator pdfTranslator;
    private final ExcelTranslator excelTranslator;
    private final PptxTranslator pptxTranslator;
    private final VisioTranslator visioTranslator;
    private final OcrTranslator ocrTranslator;


    public DocumentTranslationFacade(WordTranslator wordTranslator, PdfTranslator pdfTranslator
            , ExcelTranslator excelTranslator, PptxTranslator pptxTranslator, VisioTranslator visioTranslator,
                                     OcrTranslator ocrTranslator) {
        this.wordTranslator = wordTranslator;
        this.pdfTranslator = pdfTranslator;
        this.excelTranslator = excelTranslator;
        this.pptxTranslator = pptxTranslator;
        this.visioTranslator = visioTranslator;
        this.ocrTranslator = ocrTranslator;
    }

    public String translateFileDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return wordTranslator.translateDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pdf")) {
            return pdfTranslator.translateDocument(file, targetLanguage);
        } else if (fileName.endsWith(".pptx")) {
            return pptxTranslator.translateDocument(file, targetLanguage);
        } else if (fileName.endsWith(".xlsx")) {
            return excelTranslator.translateDocument(file, targetLanguage);
        } else if (fileName.endsWith(".vsdx")) {
            return visioTranslator.translateDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }
    }

    public String translateOcrDocument(File file, String targetLanguage) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return ocrTranslator.translateDocument(file, targetLanguage);
        } else {
            throw new IllegalArgumentException("Nieobsługiwane rozszerzenie pliku: " + fileName);
        }
    }
}
