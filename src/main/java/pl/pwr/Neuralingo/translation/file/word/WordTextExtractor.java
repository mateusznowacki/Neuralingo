package pl.pwr.Neuralingo.translation.file.word;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordTextExtractor {

    public ExtractedText extractText(File file) throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();
        int[] index = {0};

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            // 1. Body (main content)
            for (XWPFParagraph para : doc.getParagraphs()) {
                extractFromParagraph(para, paragraphs, index);
            }

            // 2. Tables in body
            for (XWPFTable table : doc.getTables()) {
                extractFromTable(table, paragraphs, index);
            }

            // 3. Headers
            for (XWPFHeader header : doc.getHeaderList()) {
                for (XWPFParagraph para : header.getParagraphs()) {
                    extractFromParagraph(para, paragraphs, index);
                }
                for (XWPFTable table : header.getTables()) {
                    extractFromTable(table, paragraphs, index);
                }
            }

            // 4. Footers
            for (XWPFFooter footer : doc.getFooterList()) {
                for (XWPFParagraph para : footer.getParagraphs()) {
                    extractFromParagraph(para, paragraphs, index);
                }
                for (XWPFTable table : footer.getTables()) {
                    extractFromTable(table, paragraphs, index);
                }
            }

            return new ExtractedText(paragraphs);

        } catch (Exception e) {
            throw new IOException("Failed to extract text from Word document", e);
        }
    }

    private void extractFromParagraph(XWPFParagraph para, List<Paragraph> paragraphs, int[] index) {
        for (XWPFRun run : para.getRuns()) {
            String text = run.text();
            if (text != null && !text.isBlank()) {
                paragraphs.add(new Paragraph(index[0]++, text.trim()));
            }
        }
    }

    private void extractFromTable(XWPFTable table, List<Paragraph> paragraphs, int[] index) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph para : cell.getParagraphs()) {
                    extractFromParagraph(para, paragraphs, index);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    extractFromTable(nestedTable, paragraphs, index);
                }
            }
        }
    }
}
