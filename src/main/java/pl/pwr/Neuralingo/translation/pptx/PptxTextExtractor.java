package pl.pwr.Neuralingo.translation.pptx;

import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PptxTextExtractor {

    public ExtractedText extractText(File inputFile) throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();
        int[] index = {0};

        try (FileInputStream fis = new FileInputStream(inputFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            List<XSLFSlide> slides = ppt.getSlides();
            for (int slideIndex = 0; slideIndex < slides.size(); slideIndex++) {
                XSLFSlide slide = slides.get(slideIndex);
                for (XSLFShape shape : slide.getShapes()) {
                    extractTextFromShape(shape, paragraphs, index, slideIndex);
                }
            }
        }

        return new ExtractedText(paragraphs);
    }

    private void extractTextFromShape(XSLFShape shape, List<Paragraph> paragraphs, int[] index, int slideIndex) {
        if (shape instanceof XSLFTextShape textShape) {
            Rectangle2D anchor = textShape.getAnchor();
            for (XSLFTextParagraph para : textShape.getTextParagraphs()) {
                for (XSLFTextRun run : para.getTextRuns()) {
                    String runText = run.getRawText();
                    if (runText != null && !runText.trim().isEmpty()) {
                        System.out.printf("Slide %d | Index %d | Text: \"%s\" | Pos: x=%.2f y=%.2f%n",
                                slideIndex, index[0], runText.trim(), anchor.getX(), anchor.getY());
                        paragraphs.add(new Paragraph(index[0]++, runText.trim(), slideIndex,
                                (int) anchor.getX(), (int) anchor.getY()));
                    }
                }
            }
        } else if (shape instanceof XSLFGroupShape group) {
            for (XSLFShape inner : group.getShapes()) {
                extractTextFromShape(inner, paragraphs, index, slideIndex);
            }
        } else if (shape instanceof XSLFTable table) {
            for (XSLFTableRow row : table.getRows()) {
                for (XSLFTableCell cell : row.getCells()) {
                    extractTextFromShape(cell, paragraphs, index, slideIndex);
                }
            }
        } else {
            // Dodaj log, jeśli pojawia się coś nietekstowego
            System.out.println("Unknown shape: " + shape.getClass().getSimpleName());
        }
    }
}
