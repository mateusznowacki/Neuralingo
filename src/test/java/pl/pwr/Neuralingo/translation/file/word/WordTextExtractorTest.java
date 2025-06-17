package pl.pwr.Neuralingo.translation.file.word;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.*;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordTextExtractorTest {

    private WordTextExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new WordTextExtractor();
    }

    @Test
    void extractText_simpleParagraphs() throws Exception {
        File tempFile = createDocxWithParagraphs("Hello", "World");

        ExtractedText extracted = extractor.extractText(tempFile);
        List<Paragraph> paragraphs = extracted.getParagraphs();

        assertEquals(2, paragraphs.size());
        assertEquals("Hello", paragraphs.get(0).getText());
        assertEquals("World", paragraphs.get(1).getText());

        tempFile.delete();
    }

    @Test
    void extractText_withTable() throws Exception {
        File tempFile = createDocxWithTable(new String[][]{
                {"Cell 1", "Cell 2"},
                {"Cell 3", "Cell 4"}
        });

        ExtractedText extracted = extractor.extractText(tempFile);
        List<Paragraph> paragraphs = extracted.getParagraphs();

        // We expect 4 paragraphs from table cells
        assertEquals(4, paragraphs.size());
        assertEquals("Cell 1", paragraphs.get(0).getText());
        assertEquals("Cell 2", paragraphs.get(1).getText());
        assertEquals("Cell 3", paragraphs.get(2).getText());
        assertEquals("Cell 4", paragraphs.get(3).getText());

        tempFile.delete();
    }

    @Test
    void extractText_withHeaderFooter() throws Exception {
        File tempFile = createDocxWithHeaderAndFooter("Header Text", "Footer Text");

        ExtractedText extracted = extractor.extractText(tempFile);
        List<Paragraph> paragraphs = extracted.getParagraphs();

        assertTrue(paragraphs.stream().anyMatch(p -> p.getText().equals("Header Text")));
        assertTrue(paragraphs.stream().anyMatch(p -> p.getText().equals("Footer Text")));

        tempFile.delete();
    }

    // --- Helper methods to create Word documents ---

    private File createDocxWithParagraphs(String... texts) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        for (String text : texts) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText(text);
        }
        return saveDocumentToTempFile(doc);
    }

    private File createDocxWithTable(String[][] data) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(data.length, data[0].length);

        for (int i = 0; i < data.length; i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < data[i].length; j++) {
                row.getCell(j).setText(data[i][j]);
            }
        }

        return saveDocumentToTempFile(doc);
    }

    private File createDocxWithHeaderAndFooter(String headerText, String footerText) throws Exception {
        XWPFDocument doc = new XWPFDocument();

        // Add header
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph headerPara = header.createParagraph();
        headerPara.createRun().setText(headerText);

        // Add footer
        XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph footerPara = footer.createParagraph();
        footerPara.createRun().setText(footerText);

        return saveDocumentToTempFile(doc);
    }

    private File saveDocumentToTempFile(XWPFDocument doc) throws Exception {
        File tempFile = Files.createTempFile("wordtextextractor-test", ".docx").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            doc.write(fos);
        }
        doc.close();
        return tempFile;
    }
}
