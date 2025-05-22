package pl.pwr.Neuralingo.translation.vsdx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class VisioTextExtractor {

    public ExtractedText extractText(File visioFile) throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();
        int index = 0;

        try (ZipFile zipFile = new ZipFile(visioFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith("visio/pages/page") && name.endsWith(".xml")) {
                    InputStream is = zipFile.getInputStream(entry);
                    String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    // szukaj tekstów w <a:t>...</a:t>
                    Pattern pattern = Pattern.compile("<a:t[^>]*>(.*?)</a:t>");
                    Matcher matcher = pattern.matcher(xml);

                    while (matcher.find()) {
                        String text = matcher.group(1).trim();
                        if (!text.isEmpty()) {
                            paragraphs.add(new Paragraph(index++, text));
                            System.out.println("📝 Extracted: " + text);
                        }
                    }
                }
            }
        }

        return new ExtractedText(paragraphs);
    }
}

