package pl.pwr.Neuralingo.translation.pdf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    public String extractLayout(File pdfFile) throws IOException, InterruptedException {
        File tempDir = pdfFile.getParentFile();

        // Nazwa wynikowego pliku .html – ten sam prefix co PDF, inna końcówka
        String htmlFileName = pdfFile.getName().replaceFirst("(?i)\\.pdf$", ".html");
        File htmlOutput = new File(tempDir, htmlFileName);

        ProcessBuilder builder = new ProcessBuilder(
                "pdf2htmlEX",
                "--embed-css", "1",
                "--embed-font", "1",
                "--embed-image", "1",
                "--embed-javascript", "1",
                "--embed-outline", "1",
                "--split-pages", "0", // <-- bardzo ważne: JEDEN HTML a nie wiele!
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

        return new String(java.nio.file.Files.readAllBytes(htmlOutput.toPath()), StandardCharsets.UTF_8);
    }

    public ExtractedText extractText(String htmlContent) {
        List<ExtractedText.Paragraph> paragraphs = new ArrayList<>();

        Document doc = Jsoup.parse(htmlContent);
        Elements divs = doc.select("div[class^=t]"); // tylko <div> których klasa zaczyna się od "t"

        int index = 0;
        for (Element div : divs) {
            String text = div.text().trim();
            if (!text.isEmpty()) {
                paragraphs.add(new ExtractedText.Paragraph(index++, text));
            }
        }

        return new ExtractedText(paragraphs);
    }
}
