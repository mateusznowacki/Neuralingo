package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


        return new String(java.nio.file.Files.readAllBytes(htmlOutput.toPath()), StandardCharsets.UTF_8);
    }


    public ExtractedText extractText(String htmlContent) {
        List<Paragraph> paragraphs = new ArrayList<>();

        Pattern divPattern = Pattern.compile("<div class=\"t[^\"]*?\">(.*?)</div>", Pattern.DOTALL);
        Matcher matcher = divPattern.matcher(htmlContent);
        int index = 0;

        while (matcher.find()) {
            String divContent = matcher.group(1);

            // Usuń wszystkie tagi, zostaw tylko tekst
            String text = divContent.replaceAll("<[^>]+>", "").trim();

            if (!text.isEmpty()) {
                paragraphs.add(new Paragraph(index++, text));
            }
        }

        return new ExtractedText(paragraphs);
    }



    private String extractVisibleTextOnly(String html) {
        StringBuilder sb = new StringBuilder();
        boolean insideTag = false;

        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);

            if (c == '<') {
                insideTag = true;
            } else if (c == '>') {
                insideTag = false;
            } else if (!insideTag) {
                sb.append(c);
            }
        }

        return sb.toString().replace('\u00A0', ' ').replaceAll(" +", " ");
    }



}
