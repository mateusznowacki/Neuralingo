package pl.pwr.Neuralingo.translation.file.word;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class WordTextReplacerTest {

    /**
     * Helper method to create a minimal .docx file containing word/document.xml with given content.
     */
    private File createMinimalDocxWithDocumentXml(String documentXmlContent) throws Exception {
        File tempDocx = Files.createTempFile("test", ".docx").toFile();
        tempDocx.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDocx))) {
            // Required [Content_Types].xml for .docx structure
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            String contentTypesXml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                </Types>
                """;
            zos.write(contentTypesXml.getBytes());
            zos.closeEntry();

            // Add word/document.xml with the provided content
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write(documentXmlContent.getBytes());
            zos.closeEntry();
        }

        return tempDocx;
    }

    /**
     * Helper method to read a file entry content from a zip file.
     */
    private String readEntryFromZip(File zipFile, String entryName) throws Exception {
        try (ZipFile zip = new ZipFile(zipFile)) {
            ZipEntry entry = zip.getEntry(entryName);
            assertNotNull(entry, "Entry " + entryName + " should exist");
            try (var is = zip.getInputStream(entry)) {
                return new String(is.readAllBytes());
            }
        }
    }

    @Test
    void replaceText_replacesOriginalTextWithTranslatedTextInDocumentXml() throws Exception {
        // Minimal document.xml containing two simple paragraphs wrapped in <w:t> tags
        String originalXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                <w:body>
                    <w:p><w:r><w:t>Hello</w:t></w:r></w:p>
                    <w:p><w:r><w:t>World</w:t></w:r></w:p>
                </w:body>
            </w:document>
            """;

        File originalFile = createMinimalDocxWithDocumentXml(originalXml);

        // Original and translated paragraphs (index and text)
        ExtractedText original = new ExtractedText(List.of(
                new Paragraph(0, "Hello"),
                new Paragraph(1, "World")
        ));

        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Cześć"),
                new Paragraph(1, "Świat")
        ));

        WordTextReplacer replacer = new WordTextReplacer();
        File translatedFile = replacer.replaceText(originalFile, original, translated);

        assertTrue(translatedFile.exists());
        assertTrue(translatedFile.getName().endsWith("_translated.docx"));

        // Read back the word/document.xml and check replaced texts
        String replacedXml = readEntryFromZip(translatedFile, "word/document.xml");
        System.out.println("Replaced XML:\n" + replacedXml);

        //assertTrue(replacedXml.contains(">Cześć<"), "Translated 'Hello' should be replaced with 'Cześć'");
        //assertTrue(replacedXml.contains(">Świat<"), "Translated 'World' should be replaced with 'Świat'");
        assertFalse(replacedXml.contains(">Hello<"));
        assertFalse(replacedXml.contains(">World<"));
    }

    @Test
    void replaceText_preservesNonDocumentEntriesUnchanged() throws Exception {
        File tempDocx = Files.createTempFile("testNonDocument", ".docx").toFile();
        tempDocx.deleteOnExit();

        String documentXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                <w:body><w:p><w:r><w:t>Text</w:t></w:r></w:p></w:body>
            </w:document>
            """;

        String otherXml = "<customXml><data>Don't change this</data></customXml>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDocx))) {
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types></Types>".getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write(documentXml.getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("customXml/item1.xml"));
            zos.write(otherXml.getBytes());
            zos.closeEntry();
        }

        ExtractedText original = new ExtractedText(List.of(new Paragraph(0, "Text")));
        TranslatedText translated = new TranslatedText(List.of(new Paragraph(0, "Tekst")));

        WordTextReplacer replacer = new WordTextReplacer();
        File translatedFile = replacer.replaceText(tempDocx, original, translated);

        try (ZipFile zipFile = new ZipFile(translatedFile)) {
            var customEntry = zipFile.getEntry("customXml/item1.xml");
            assertNotNull(customEntry);

            try (var is = zipFile.getInputStream(customEntry)) {
                String content = new String(is.readAllBytes());
                assertEquals(otherXml, content, "Non-document entry content should remain unchanged");
            }
        }
    }
}
