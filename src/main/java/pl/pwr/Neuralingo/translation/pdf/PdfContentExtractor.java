package pl.pwr.Neuralingo.translation.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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



        ProcessBuilder builder = new ProcessBuilder(
                "pdf2htmlEX",
                "--embed", "cfijo",
                "--embed-external-font", "1",
                "--dest-dir", tempDir.getAbsolutePath(),
                pdfFile.getAbsolutePath()
        );

        builder.redirectErrorStream(true);
        Process process = builder.start();

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

        return new String(java.nio.file.Files.readAllBytes(htmlOutput.toPath()), StandardCharsets.UTF_8);
    }


    public ExtractedText extractText(File pdfFile) throws IOException {
        List<ExtractedText.Paragraph> paragraphs = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document); // całość tekstu z PDF

            String[] lines = rawText.split("\\r?\\n"); // każda linia osobno
            int index = 1;

            for (String line : lines) {
                // Nie usuwamy linii pustych, tylko je pomijamy przy zapisie do JSON
                String trimmed = line.strip();
                if (!trimmed.isEmpty()) {
                    paragraphs.add(new ExtractedText.Paragraph(index++, trimmed));
                }
            }
        }

        ExtractedText extractedText = new ExtractedText(paragraphs);

        // Zapisz do JSON (plik o tej samej nazwie co PDF + .json)
        File outputJson = new File(pdfFile.getParent(), pdfFile.getName().replaceFirst("(?i)\\.pdf$", ".json"));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputJson, extractedText);

        return extractedText;
    }


}
