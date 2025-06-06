package pl.pwr.Neuralingo.translation.file.pdf;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HtmlToPdfConverter {

    private final Path scriptsDir = Paths.get(".");

    public File convertHtmlToPdf(String htmlContent, File outputPdfFile) throws IOException, InterruptedException {
        // 1. Utwórz tymczasowy plik HTML w tym samym katalogu co wynikowy PDF
        Path tempHtmlPath = outputPdfFile.toPath().resolveSibling(
                outputPdfFile.getName().replaceAll("\\.pdf$", ".html")
        );
        Files.writeString(tempHtmlPath, htmlContent, StandardCharsets.UTF_8);
        // 2. Uruchom Puppeteer z node.js
        ProcessBuilder pb = new ProcessBuilder(
                "node",
                "html2pdf.js",
                tempHtmlPath.toAbsolutePath().toString(),
                outputPdfFile.getAbsolutePath()
        );
        pb.directory(scriptsDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("❌ Puppeteer failed, exit code: " + exitCode);
        }
        if (!Files.exists(outputPdfFile.toPath())) {
            throw new FileNotFoundException("❌ PDF not created at: " + outputPdfFile);
        }

        // 3. Usuń plik tymczasowy
        Files.deleteIfExists(tempHtmlPath);
        return outputPdfFile;
    }
}
