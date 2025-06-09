package pl.pwr.Neuralingo.translation.ocr;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Component
public class WordToPdfConverter {

    public File convertWordToPdf(File inputDocxFile) {
        try {
            // Wczytaj dokument Word
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputDocxFile);

            // Przygotuj plik wyjściowy (PDF)
            String outputPdfPath = inputDocxFile.getParent() + File.separator +
                    inputDocxFile.getName().replaceFirst("\\.docx$", "") + ".pdf";
            File outputPdfFile = new File(outputPdfPath);

            // Użyj OutputStream do zapisania PDF
            try (OutputStream os = new FileOutputStream(outputPdfFile)) {
                Docx4J.toPDF(wordMLPackage, os);
            }

            return outputPdfFile;
        } catch (Docx4JException e) {
            throw new RuntimeException("Błąd podczas konwersji Word -> PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Inny błąd przy konwersji Word -> PDF: " + e.getMessage(), e);
        }
    }
}
