package pl.pwr.Neuralingo.translation.file.vsdx;

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
public class VisioTextReplacer {

    public File replaceText(File originalFile, ExtractedText original, TranslatedText translated) throws IOException {
        String outputFileName = originalFile.getName().replaceFirst("(?i)\\.vsdx$", "_translated.vsdx");
        File resultFile = new File(originalFile.getParent(), outputFileName);

        try (ZipFile zipFile = new ZipFile(originalFile);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resultFile))) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream is = zipFile.getInputStream(entry);
                byte[] data;

                if (entry.getName().startsWith("visio/pages/page") && entry.getName().endsWith(".xml")) {
                    String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    List<Paragraph> originalParagraphs = original.getParagraphs();
                    List<Paragraph> translatedParagraphs = translated.getParagraphs();

                    for (int i = 0; i < Math.min(originalParagraphs.size(), translatedParagraphs.size()); i++) {
                        String originalText = originalParagraphs.get(i).getText();
                        String translatedText = translatedParagraphs.get(i).getText().trim();
                        if (originalText == null || originalText.trim().isEmpty() || translatedText.isEmpty()) continue;

                        String escapedOriginal = Pattern.quote(originalText.trim());
                        Pattern pattern = Pattern.compile(">(\\s*)(" + escapedOriginal + ")(\\s*)<");
                        Matcher matcher = pattern.matcher(xml);
                        StringBuffer sb = new StringBuffer();
                        boolean replaced = false;


                        while (matcher.find()) {
                            String replacement = matcher.group(1) + preserveSpaces(matcher.group(2) + originalText + matcher.group(4), translatedText) + matcher.group(5);
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                            replaced = true;

                            System.out.println("🔁 Podmieniono w pliku XML [" + entry.getName() + "]:");
                            System.out.println("   ORYGINAŁ   → \"" + originalText + "\"");
                            System.out.println("   TŁUMACZENIE → \"" + translatedText + "\"");

                            break; // tylko jedna zamiana na parę
                        }
                        if (replaced) {
                            matcher.appendTail(sb);
                            xml = sb.toString();
                        } else {
                            System.out.println("⚠️ NIE ZNALEZIONO regexem w [" + entry.getName() + "]: \"" + originalText + "\"");
                        }


                        if (replaced) {
                            matcher.appendTail(sb);
                            xml = sb.toString();
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

    private String preserveSpaces(String original, String translated) {
        if (original.startsWith(" ") && !translated.startsWith(" ")) {
            translated = " " + translated;
        }
        if (original.endsWith(" ") && !translated.endsWith(" ")) {
            translated = translated + " ";
        }
        return translated;
    }
}
