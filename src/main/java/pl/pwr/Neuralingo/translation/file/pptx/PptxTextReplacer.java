package pl.pwr.Neuralingo.translation.file.pptx;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Component
public class PptxTextReplacer {

    public File replaceText(File originalFile, ExtractedText original, TranslatedText translated) throws IOException {
        String translatedName = originalFile.getName().replaceFirst("(?i)\\.pptx$", "_translated.pptx");
        File resultFile = new File(originalFile.getParent(), translatedName);


        try (ZipFile zipFile = new ZipFile(originalFile);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resultFile))) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream is = zipFile.getInputStream(entry);
                byte[] data;

                if (entry.getName().startsWith("ppt/slides/slide") && entry.getName().endsWith(".xml")) {
                    String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    List<Paragraph> originalParagraphs = original.getParagraphs();
                    List<Paragraph> translatedParagraphs = translated.getParagraphs();

                    for (int i = 0; i < Math.min(originalParagraphs.size(), translatedParagraphs.size()); i++) {
                        String originalText = originalParagraphs.get(i).getText();
                        String translatedText = translatedParagraphs.get(i).getText().trim();

                        if (originalText == null || originalText.trim().isEmpty() || translatedText.isEmpty())
                            continue;

                        String escapedOriginal = Pattern.quote(originalText.trim());
                        Pattern pattern = Pattern.compile(">(\\s*)(" + escapedOriginal + ")(\\s*)<");
                        Matcher matcher = pattern.matcher(xml);

                        if (matcher.find()) {
                            int end = matcher.end();

                            // Sprawdzenie tylko końca – czy po dopasowaniu nie ma spacji ani znacznika
                            boolean needsTrailingSpace = false;
                            if (end < xml.length()) {
                                char nextChar = xml.charAt(end);
                                if (nextChar != '<' && nextChar != ' ') {
                                    needsTrailingSpace = true;
                                }
                            }

                            if ((originalText.endsWith(" ") && !translatedText.endsWith(" ")) || needsTrailingSpace) {
                                translatedText += " ";
                            }

                            String replacement = ">" + Matcher.quoteReplacement(translatedText) + "<";
                            xml = matcher.replaceFirst(replacement);
                        }
                    }

                    data = xml.getBytes(StandardCharsets.UTF_8);
                } else {
                    data = is.readAllBytes();
                }

                zos.putNextEntry(new ZipEntry(entry.getName()));
                zos.write(data);
                zos.closeEntry();
            }
        }

        return resultFile;
    }

}