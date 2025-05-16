package pl.pwr.Neuralingo.translation.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordTextExtractor {

    public ExtractedText extractText(File file) throws IOException {
        List<ExtractedText.Paragraph> paragraphList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (int i = 0; i < paragraphs.size(); i++) {
                String text = paragraphs.get(i).getText();
                if (text != null && !text.isBlank()) {
                    paragraphList.add(new ExtractedText.Paragraph(i, text));
                }
            }
        }

        return new ExtractedText(paragraphList);
    }


}
