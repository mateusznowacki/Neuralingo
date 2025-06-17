package pl.pwr.Neuralingo.translation.file.pptx;

import org.apache.poi.xslf.usermodel.*;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PptxTextExtractorTest {

    private final PptxTextExtractor extractor = new PptxTextExtractor();

    @Test
    void extractText_fromTextBox() throws Exception {
        File file = createTempPptxFile("Hello world");

        ExtractedText result = extractor.extractText(file);
        List<Paragraph> paragraphs = result.getParagraphs();

        assertEquals(1, paragraphs.size());
        assertEquals("Hello world", paragraphs.get(0).getText());
        assertEquals(0, paragraphs.get(0).getIndex());
        assertEquals(null, paragraphs.get(0).getSlideIndex());
    }

    @Test
    void extractText_fromTable() throws Exception {
        File tempFile = File.createTempFile("table", ".pptx");
        tempFile.deleteOnExit();

        try (XMLSlideShow ppt = new XMLSlideShow(); FileOutputStream out = new FileOutputStream(tempFile)) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTable table = slide.createTable();
            table.setAnchor(new Rectangle2D.Double(100, 100, 400, 300));

            XSLFTableRow row = table.addRow();
            XSLFTableCell cell = row.addCell();
            cell.setText("Cell text");

            ppt.write(out);
        }

        ExtractedText result = extractor.extractText(tempFile);
        List<Paragraph> paragraphs = result.getParagraphs();

        assertEquals(1, paragraphs.size());
        assertEquals("Cell text", paragraphs.get(0).getText());
    }

    @Test
    void extractText_fromGroupShape() throws Exception {
        File tempFile = File.createTempFile("group", ".pptx");
        tempFile.deleteOnExit();

        try (XMLSlideShow ppt = new XMLSlideShow(); FileOutputStream out = new FileOutputStream(tempFile)) {
            XSLFSlide slide = ppt.createSlide();
            XSLFGroupShape group = slide.createGroup();

            XSLFTextBox textBox = group.createTextBox();
            textBox.setAnchor(new Rectangle2D.Double(50, 50, 200, 100));
            textBox.setText("Grouped text");

            ppt.write(out);
        }

        ExtractedText result = extractor.extractText(tempFile);
        List<Paragraph> paragraphs = result.getParagraphs();

        assertEquals(1, paragraphs.size());
        assertEquals("Grouped text", paragraphs.get(0).getText());
    }

    @Test
    void extractText_fromEmptySlide_returnsNoParagraphs() throws Exception {
        File tempFile = File.createTempFile("empty", ".pptx");
        tempFile.deleteOnExit();

        try (XMLSlideShow ppt = new XMLSlideShow(); FileOutputStream out = new FileOutputStream(tempFile)) {
            ppt.createSlide(); // empty slide
            ppt.write(out);
        }

        ExtractedText result = extractor.extractText(tempFile);
        assertTrue(result.getParagraphs().isEmpty());
    }

    private File createTempPptxFile(String text) throws Exception {
        File tempFile = File.createTempFile("test", ".pptx");
        tempFile.deleteOnExit();

        try (XMLSlideShow ppt = new XMLSlideShow(); FileOutputStream out = new FileOutputStream(tempFile)) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setAnchor(new Rectangle2D.Double(100, 150, 300, 100));
            textBox.setText(text);
            ppt.write(out);
        }

        return tempFile;
    }
}
