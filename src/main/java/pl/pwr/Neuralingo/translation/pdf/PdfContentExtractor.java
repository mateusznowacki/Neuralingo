package pl.pwr.Neuralingo.translation.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfContentExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractLayout(File pdfFile) throws IOException, InterruptedException {
        File htmlOutput = new File(pdfFile.getParent(), pdfFile.getName() + ".html");
        File tempDir = pdfFile.getParentFile();
        FileUtils.cleanDirectory(tempDir); // usuwa wszystko z folderu przed konwersją

        ProcessBuilder builder = new ProcessBuilder(
                "pdf2htmlEX",
                "--embed", "cfijo",
                "--embed-external-font", "1",
                "--dest-dir", tempDir.getAbsolutePath(),
                "--output", "converted.html",
                pdfFile.getAbsolutePath()
        );

        builder.redirectErrorStream(true);
        Process process = builder.start();

        // Log output (opcjonalnie)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[pdf2htmlEX] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("pdf2htmlEX failed with exit code " + exitCode);
        }

        // Wczytaj HTML do Stringa
        return new String(java.nio.file.Files.readAllBytes(htmlOutput.toPath()), StandardCharsets.UTF_8);
    }

    public ExtractedText extractText(File pdfFile) throws IOException {
        List<ExtractedText.Paragraph> paragraphs = new ArrayList<>();
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfFile))) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                String content = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
                paragraphs.add(new ExtractedText.Paragraph(i, content));
            }
        }

        ExtractedText extractedText = new ExtractedText(paragraphs);

        // Zapis do pliku JSON tymczasowo w tym samym folderze co PDF
        File outputJson = new File(pdfFile.getParent(), pdfFile.getName() + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputJson, extractedText);

        return extractedText;
    }
}
