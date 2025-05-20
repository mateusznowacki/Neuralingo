package pl.pwr.Neuralingo.translation.word;

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
        List<Paragraph> paragraphList = new ArrayList<>();
        int index = 0;

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            // Akapity główne
            index = extractParagraphs(document.getParagraphs(), paragraphList, index);

            // Tabele
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        index = extractParagraphs(cell.getParagraphs(), paragraphList, index);
                    }
                }
            }

            // Nagłówki
            for (XWPFHeader header : document.getHeaderList()) {
                index = extractParagraphs(header.getParagraphs(), paragraphList, index);
            }

            // Stopki
            for (XWPFFooter footer : document.getFooterList()) {
                index = extractParagraphs(footer.getParagraphs(), paragraphList, index);
            }

            // Przypisy dolne
            for (XWPFFootnote footnote : document.getFootnotes()) {
                index = extractParagraphs(footnote.getParagraphs(), paragraphList, index);
            }
        }

        return new ExtractedText(paragraphList);
    }

    private int extractParagraphs(List<XWPFParagraph> paragraphs, List<Paragraph> paragraphList, int startIndex) {
        int index = startIndex;
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText();
            if (text != null && !text.isBlank()) {
                paragraphList.add(new Paragraph(index++, text));
            }
        }
        return index;
    }
}
