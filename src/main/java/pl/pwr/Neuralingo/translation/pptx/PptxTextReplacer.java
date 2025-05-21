package pl.pwr.Neuralingo.translation.pptx;

import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PptxTextReplacer {

    public File replaceText(File originalFile, ExtractedText original, TranslatedText translated) throws IOException {
        Map<Integer, String> translationMap = translated.getParagraphs().stream()
                .collect(Collectors.toMap(Paragraph::getIndex, Paragraph::getText));

        String outputPath = originalFile.getAbsolutePath().replace(".pptx", "") + "_translated.pptx";
        File outputFile = new File(outputPath);

        try (FileInputStream fis = new FileInputStream(originalFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            int[] index = {0};
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    applyTranslationToShape(shape, translationMap, index);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                ppt.write(fos);
            }
        }

        return outputFile;
    }

    private void applyTranslationToShape(XSLFShape shape, Map<Integer, String> translationMap, int[] index) {
        if (shape instanceof XSLFTextShape textShape) {
            if (translationMap.containsKey(index[0])) {
                String newText = translationMap.get(index[0]);
                if (!textShape.getTextParagraphs().isEmpty()) {
                    XSLFTextParagraph para = textShape.getTextParagraphs().get(0);
                    if (!para.getTextRuns().isEmpty()) {
                        para.getTextRuns().get(0).setText(newText);
                    } else {
                        para.addNewTextRun().setText(newText);
                    }
                } else {
                    textShape.setText(newText);
                }
            }
            index[0]++;
        } else if (shape instanceof XSLFGroupShape group) {
            for (XSLFShape inner : group.getShapes()) {
                applyTranslationToShape(inner, translationMap, index);
            }
        } else if (shape instanceof XSLFTable table) {
            for (XSLFTableRow row : table.getRows()) {
                for (XSLFTableCell cell : row.getCells()) {
                    applyTranslationToShape(cell, translationMap, index);
                }
            }
        }
    }
}
