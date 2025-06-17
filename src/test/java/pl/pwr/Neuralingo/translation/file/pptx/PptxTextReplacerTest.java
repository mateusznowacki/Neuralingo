package pl.pwr.Neuralingo.translation.file.pptx;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PptxTextReplacerTest {

    private final PptxTextReplacer replacer = new PptxTextReplacer();

    @Test
    void replaceText_shouldReplaceXmlTextInSlide() throws Exception {
        // Arrange: Create mock PPTX zip with a slide XML entry containing specific text
        File original = createMockPptxWithXml("ppt/slides/slide1.xml", "<a:t>Hello</a:t>");
        Paragraph originalPara = new Paragraph(0, "Hello");
        Paragraph translatedPara = new Paragraph(0, "Bonjour");

        ExtractedText extractedText = new ExtractedText(List.of(originalPara));
        TranslatedText translatedText = new TranslatedText(List.of(translatedPara));

        // Act
        File result = replacer.replaceText(original, extractedText, translatedText);

        // Assert
        try (ZipFile zipFile = new ZipFile(result)) {
            String xml = readEntryAsString(zipFile, "ppt/slides/slide1.xml");
            assertTrue(xml.contains("Bonjour"));
            assertFalse(xml.contains("Hello"));
        }
    }

    @Test
    void replaceText_shouldPreserveOtherEntries() throws Exception {
        File original = createMockPptxWithXml("ppt/slides/slide1.xml", "<a:t>Text</a:t>");
        ExtractedText extractedText = new ExtractedText(List.of());
        TranslatedText translatedText = new TranslatedText(List.of());

        File result = replacer.replaceText(original, extractedText, translatedText);

        try (ZipFile zipFile = new ZipFile(result)) {
            assertNotNull(zipFile.getEntry("ppt/slides/slide1.xml"));
        }
    }

    @Test
    void replaceText_shouldSkipNonMatchingText() throws Exception {
        File original = createMockPptxWithXml("ppt/slides/slide1.xml", "<a:t>Unchanged</a:t>");

        ExtractedText extracted = new ExtractedText(List.of(new Paragraph(0, "Different")));
        TranslatedText translated = new TranslatedText(List.of(new Paragraph(0, "Différent")));

        File result = replacer.replaceText(original, extracted, translated);

        try (ZipFile zipFile = new ZipFile(result)) {
            String xml = readEntryAsString(zipFile, "ppt/slides/slide1.xml");
            assertTrue(xml.contains("Unchanged"));
            assertFalse(xml.contains("Différent"));
        }
    }

    // === Helpers ===

    private File createMockPptxWithXml(String entryName, String content) throws IOException {
        File file = File.createTempFile("test", ".pptx");
        file.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + content).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return file;
    }

    private String readEntryAsString(ZipFile zipFile, String entryName) throws IOException {
        try (InputStream is = zipFile.getInputStream(zipFile.getEntry(entryName))) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
