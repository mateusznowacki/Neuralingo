package pl.pwr.Neuralingo.translation.word;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WordTextReplacer {

    public File replaceParagraphs(File originalFile,
                                  ExtractedText original,
                                  TranslatedText translated) throws IOException {

        Map<Integer, String> translatedMap = translated.paragraphs.stream()
                .collect(Collectors.toMap(p -> p.index, p -> p.text));

        String outputPath = originalFile.getAbsolutePath().replace(".docx", "") + "_translated.docx";
        File outputFile = new File(outputPath);

        try (FileInputStream fis = new FileInputStream(originalFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            int[] currentIndex = {0};

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceTextIfExists(paragraph, translatedMap, currentIndex);
            }

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceTextIfExists(paragraph, translatedMap, currentIndex);
                        }
                    }
                }
            }

            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    replaceTextIfExists(paragraph, translatedMap, currentIndex);
                }
            }

            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    replaceTextIfExists(paragraph, translatedMap, currentIndex);
                }
            }

            for (XWPFFootnote footnote : document.getFootnotes()) {
                for (XWPFParagraph paragraph : footnote.getParagraphs()) {
                    replaceTextIfExists(paragraph, translatedMap, currentIndex);
                }
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }

        return outputFile;
    }

    private void replaceTextIfExists(XWPFParagraph paragraph, Map<Integer, String> map, int[] index) {
        if (map.containsKey(index[0])) {
            String newText = map.get(index[0]);
            List<XWPFRun> runs = paragraph.getRuns();
            if (!runs.isEmpty()) {
                runs.get(0).setText(newText, 0);
                for (int i = 1; i < runs.size(); i++) {
                    runs.get(i).setText("", 0);
                }
            } else {
                XWPFRun run = paragraph.createRun();
                run.setText(newText);
            }
        }
        index[0]++;
    }
}
