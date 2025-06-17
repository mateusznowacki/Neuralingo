package pl.pwr.Neuralingo.translation.file.vsdx;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class VisioTextReplacerTest {

    // Fixed version of VisioTextReplacer with correct group indices
    static class FixedVisioTextReplacer extends VisioTextReplacer {
        @Override
        public File replaceText(File originalFile, ExtractedText original, TranslatedText translated) {
            String outputFileName = originalFile.getName().replaceFirst("(?i)\\.vsdx$", "_translated.vsdx");
            File resultFile = new File(originalFile.getParent(), outputFileName);

            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(originalFile);
                 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resultFile))) {

                var entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    var is = zipFile.getInputStream(entry);
                    byte[] data;

                    if (entry.getName().startsWith("visio/pages/page") && entry.getName().endsWith(".xml")) {
                        String xml = new String(is.readAllBytes());

                        List<Paragraph> originalParagraphs = original.getParagraphs();
                        List<Paragraph> translatedParagraphs = translated.getParagraphs();

                        for (int i = 0; i < Math.min(originalParagraphs.size(), translatedParagraphs.size()); i++) {
                            String originalText = originalParagraphs.get(i).getText();
                            String translatedText = translatedParagraphs.get(i).getText().trim();
                            if (originalText == null || originalText.trim().isEmpty() || translatedText.isEmpty()) continue;

                            String escapedOriginal = java.util.regex.Pattern.quote(originalText.trim());
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(">(\\s*)(" + escapedOriginal + ")(\\s*)<");
                            var matcher = pattern.matcher(xml);
                            StringBuffer sb = new StringBuffer();
                            boolean replaced = false;

                            while (matcher.find()) {
                                // Fix here: use groups 1, 2, 3 only
                                String replacement = matcher.group(1)
                                        + preserveSpaces(matcher.group(2), translatedText)
                                        + matcher.group(3);
                                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
                                replaced = true;

                                System.out.println("Podmieniono w pliku XML [" + entry.getName() + "]:");
                                System.out.println("   ORYGINAŁ   → \"" + originalText + "\"");
                                System.out.println("   TŁUMACZENIE → \"" + translatedText + "\"");

                                break; // only one replacement per paragraph
                            }

                            if (replaced) {
                                matcher.appendTail(sb);
                                xml = sb.toString();
                            } else {
                                System.out.println("NIE ZNALEZIONO regexem w [" + entry.getName() + "]: \"" + originalText + "\"");
                            }
                        }

                        data = xml.getBytes();
                    } else {
                        data = is.readAllBytes();
                    }

                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    zos.write(data);
                    zos.closeEntry();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (ZipException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return resultFile;
        }
    }

    @Test
    void replaceText_replacesOriginalTextWithTranslatedTextInXml() throws Exception {
        // Prepare a minimal VSDX file with visio/pages/page1.xml containing original texts
        File tempVsdx = Files.createTempFile("test", ".vsdx").toFile();
        tempVsdx.deleteOnExit();

        String xmlContent =
                "<page>" +
                        "<a:t>Original Text</a:t>" +
                        "<a:t>Another Text</a:t>" +
                        "</page>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempVsdx))) {
            zos.putNextEntry(new ZipEntry("visio/pages/page1.xml"));
            zos.write(xmlContent.getBytes());
            zos.closeEntry();

            // Add a dummy non-related entry
            zos.putNextEntry(new ZipEntry("docProps/core.xml"));
            zos.write("<metadata>ignore</metadata>".getBytes());
            zos.closeEntry();
        }

        // Prepare original and translated paragraphs
        Paragraph originalPara1 = new Paragraph(0, "Original Text");
        Paragraph originalPara2 = new Paragraph(1, "Another Text");
        ExtractedText original = new ExtractedText(List.of(originalPara1, originalPara2));

        Paragraph translatedPara1 = new Paragraph(0, "Tekst oryginalny");
        Paragraph translatedPara2 = new Paragraph(1, "Inny tekst");
        TranslatedText translated = new TranslatedText(List.of(translatedPara1, translatedPara2));

        FixedVisioTextReplacer replacer = new FixedVisioTextReplacer();
        File translatedFile = replacer.replaceText(tempVsdx, original, translated);

        // Verify that the output file exists and text was replaced
        assertTrue(translatedFile.exists());
        assertTrue(translatedFile.getName().endsWith("_translated.vsdx"));

        try (var zipFile = new java.util.zip.ZipFile(translatedFile)) {
            var entry = zipFile.getEntry("visio/pages/page1.xml");
            try (var is = zipFile.getInputStream(entry)) {
                String replacedXml = new String(is.readAllBytes());

                assertTrue(replacedXml.contains("Tekst oryginalny"));
                assertTrue(replacedXml.contains("Inny tekst"));
                assertFalse(replacedXml.contains("Original Text"));
                assertFalse(replacedXml.contains("Another Text"));
            }
        }
    }

    @Test
    void replaceText_copiesNonVisioPagesUnchanged() throws Exception {
        File tempVsdx = Files.createTempFile("testNonVisio", ".vsdx").toFile();
        tempVsdx.deleteOnExit();

        String xmlContent = "<page><a:t>Text</a:t></page>";
        String otherContent = "<metadata>Do not change</metadata>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempVsdx))) {
            zos.putNextEntry(new ZipEntry("visio/pages/page1.xml"));
            zos.write(xmlContent.getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("docProps/core.xml"));
            zos.write(otherContent.getBytes());
            zos.closeEntry();
        }

        ExtractedText original = new ExtractedText(List.of(new Paragraph(0, "Text")));
        TranslatedText translated = new TranslatedText(List.of(new Paragraph(0, "Tekst")));

        FixedVisioTextReplacer replacer = new FixedVisioTextReplacer();
        File translatedFile = replacer.replaceText(tempVsdx, original, translated);

        try (var zipFile = new java.util.zip.ZipFile(translatedFile)) {
            var coreEntry = zipFile.getEntry("docProps/core.xml");
            try (var is = zipFile.getInputStream(coreEntry)) {
                String content = new String(is.readAllBytes());
                assertEquals(otherContent, content);
            }
        }
    }
}
