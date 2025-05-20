package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class HtmlToPdfConverter {
    private final Path scriptsDir = Paths.get("scripts"); // katalog z html2pdf.js

    public File convertHtmlToPdf(File inputHtmlFile, File outputPdfFile) throws IOException, InterruptedException {
        // 1. Skopiuj plik HTML do scripts/input.html
        Path inputHtmlPath = scriptsDir.resolve("input.html");
        Files.copy(inputHtmlFile.toPath(), inputHtmlPath, StandardCopyOption.REPLACE_EXISTING);

        // 2. Uruchom Puppeteer (html2pdf.js)
        ProcessBuilder pb = new ProcessBuilder("node", "html2pdf.js");
        pb.directory(scriptsDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 3. Debug: wypisz stdout Puppeteera
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("❌ Puppeteer HTML→PDF failed, exit code: " + exitCode);
        }

        // 4. Sprawdź czy plik został wygenerowany
        Path generatedPdfPath = scriptsDir.resolve("output.pdf");
        if (!Files.exists(generatedPdfPath)) {
            throw new FileNotFoundException("❌ Nie znaleziono output.pdf po konwersji");
        }

        // 5. Skopiuj output.pdf w docelowe miejsce
        Files.copy(generatedPdfPath, outputPdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return outputPdfFile;
    }
}

