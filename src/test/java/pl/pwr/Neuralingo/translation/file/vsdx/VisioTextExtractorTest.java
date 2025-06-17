package pl.pwr.Neuralingo.translation.file.vsdx;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class VisioTextExtractorTest {

    @Test
    void extractText_shouldExtractAllTextFromXmlPages() throws Exception {
        // Prepare a temp VSDX-like ZIP file with a single page XML containing <a:t> tags
        File tempVsdx = Files.createTempFile("test", ".vsdx").toFile();
        tempVsdx.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempVsdx))) {
            // Add a dummy page xml entry
            zos.putNextEntry(new ZipEntry("visio/pages/page1.xml"));
            String xmlContent =
                    "<page>" +
                            "<a:t>First Text</a:t>" +
                            "<a:t> Second Text </a:t>" +  // With spaces to test trim
                            "<a:t></a:t>" +               // Empty text should be skipped
                            "</page>";
            zos.write(xmlContent.getBytes());
            zos.closeEntry();

            // Add another unrelated file (should be ignored)
            zos.putNextEntry(new ZipEntry("docProps/core.xml"));
            zos.write("<metadata>ignored</metadata>".getBytes());
            zos.closeEntry();
        }

        VisioTextExtractor extractor = new VisioTextExtractor();
        ExtractedText extracted = extractor.extractText(tempVsdx);

        List<Paragraph> paragraphs = extracted.getParagraphs();
        assertEquals(2, paragraphs.size());

        assertEquals("First Text", paragraphs.get(0).getText());
        assertEquals("Second Text", paragraphs.get(1).getText());

        // Indexes should be incremental
        assertEquals(0, paragraphs.get(0).getIndex());
        assertEquals(1, paragraphs.get(1).getIndex());
    }

    @Test
    void extractText_shouldReturnEmptyIfNoMatchingEntries() throws Exception {
        File tempVsdx = Files.createTempFile("empty", ".vsdx").toFile();
        tempVsdx.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempVsdx))) {
            zos.putNextEntry(new ZipEntry("not-visio/something.xml"));
            zos.write("<xml>no a:t here</xml>".getBytes());
            zos.closeEntry();
        }

        VisioTextExtractor extractor = new VisioTextExtractor();
        ExtractedText extracted = extractor.extractText(tempVsdx);

        assertTrue(extracted.getParagraphs().isEmpty());
    }
}
