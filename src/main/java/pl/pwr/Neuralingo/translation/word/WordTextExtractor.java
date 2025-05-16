package pl.pwr.Neuralingo.translation.word;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordTextExtractor {

    public ExtractedText extractText(File file) throws IOException {
        List<ExtractedText.Paragraph> paragraphList = new ArrayList<>();
        final int[] index = {0};

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            // Akapity
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    paragraphList.add(new ExtractedText.Paragraph(index[0]++, text));
                }
            }

            // Tabele
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            String text = p.getText();
                            if (text != null && !text.isBlank()) {
                                paragraphList.add(new ExtractedText.Paragraph(index[0]++, text));
                            }
                        }
                    }
                }
            }

            // Nagłówki
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    String text = paragraph.getText();
                    if (text != null && !text.isBlank()) {
                        paragraphList.add(new ExtractedText.Paragraph(index[0]++, text));
                    }
                }
            }

            // Stopki
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    String text = paragraph.getText();
                    if (text != null && !text.isBlank()) {
                        paragraphList.add(new ExtractedText.Paragraph(index[0]++, text));
                    }
                }
            }


            // Przypisy dolne (footnotes)
            for (XWPFFootnote footnote : document.getFootnotes()) {
                for (XWPFParagraph p : footnote.getParagraphs()) {
                    String text = p.getText();
                    if (text != null && !text.isBlank()) {
                        paragraphList.add(new ExtractedText.Paragraph(index[0]++, text));
                    }
                }
            }
        }

        return new ExtractedText(paragraphList);
    }
}
