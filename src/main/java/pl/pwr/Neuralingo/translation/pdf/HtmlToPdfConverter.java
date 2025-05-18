package pl.pwr.Neuralingo.translation.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class HtmlToPdfConverter {
    public File convertHtmlToPdf(File translatedHtmlFile) {
        try {
            // Wyznacz plik wyjściowy
            File outputPdf = new File(
                    translatedHtmlFile.getParentFile(),
                    translatedHtmlFile.getName().replaceFirst("(?i)\\.html$", "_converted.pdf")
            );

            // Wczytaj zawartość HTML jako string
            String htmlContent = new String(
                    new FileInputStream(translatedHtmlFile).readAllBytes(),
                    StandardCharsets.UTF_8
            );

            // Konwertuj do PDF
            try (FileOutputStream fos = new FileOutputStream(outputPdf)) {
                ConverterProperties props = new ConverterProperties();
                HtmlConverter.convertToPdf(htmlContent, fos, props);
            }

            System.out.println("✅ PDF zapisany: " + outputPdf.getAbsolutePath());
            return outputPdf;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Błąd konwersji HTML → PDF", e);
        }
    }
}

