package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class HtmlToPdfConverter {

    private final Path scriptsDir = Paths.get("scripts");

    public File convertHtmlToPdf(String htmlContent, File outputPdfFile) throws IOException, InterruptedException {
        // 1. Utwórz tymczasowy plik HTML
        String baseName = outputPdfFile.getName().replaceAll("_translated\\.pdf$", "_translated.html");
        Path tempHtmlPath = scriptsDir.resolve(baseName);
        Files.writeString(tempHtmlPath, htmlContent, StandardCharsets.UTF_8);

        // 2. Wyjściowy PDF w tym samym katalogu
        Path generatedPdfPath = scriptsDir.resolve(baseName.replaceAll("\\.html$", ".pdf"));

        // 3. Wywołaj Puppeteera (html2pdf.js)
        ProcessBuilder pb = new ProcessBuilder(
                "node",
                "html2pdf.js",
                tempHtmlPath.toAbsolutePath().toString(),
                generatedPdfPath.toAbsolutePath().toString()
        );

        pb.directory(scriptsDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("❌ Puppeteer PDF generation failed, exit code: " + exitCode);
        }

        if (!Files.exists(generatedPdfPath)) {
            throw new FileNotFoundException("❌ Nie znaleziono PDF: " + generatedPdfPath);
        }

        // 4. Kopiuj PDF do miejsca docelowego
        Files.copy(generatedPdfPath, outputPdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return outputPdfFile;
    }
}
