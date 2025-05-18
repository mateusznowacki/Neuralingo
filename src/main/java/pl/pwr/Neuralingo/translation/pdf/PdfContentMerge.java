package pl.pwr.Neuralingo.translation.pdf;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class PdfContentMerge {

    public ExtractedText extractFromHtml(File htmlFile) throws IOException {
        Document doc = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());
        Elements textElements = doc.select("div.t");

        // Grupujemy tekst po Y (wiersze), a potem sortujemy po X (kolejność w wierszu)
        Map<String, List<Element>> linesByY = new TreeMap<>();

        for (Element el : textElements) {
            Optional<String> yClass = Arrays.stream(el.className().split(" "))
                    .filter(cls -> cls.matches("y[0-9a-f]+"))
                    .findFirst();
            if (yClass.isEmpty()) continue;

            linesByY.computeIfAbsent(yClass.get(), k -> new ArrayList<>()).add(el);
        }

        List<ExtractedText.Paragraph> paragraphs = new ArrayList<>();
        int index = 1;

        for (var entry : linesByY.entrySet()) {
            List<Element> lineElements = entry.getValue();
            lineElements.sort(Comparator.comparingInt(el -> extractX(el.className())));

            List<String> cells = new ArrayList<>();
            for (Element cell : lineElements) {
                String text = cell.text().trim();
                if (!text.isEmpty()) {
                    cells.add(text);
                }
            }

            if (!cells.isEmpty()) {
                // Jeśli linia wygląda na tabelę (więcej niż 1 komórka i brak przecinków łączących), to potraktuj jako wiersz tabeli
                boolean isTableRow = cells.size() > 1;
                if (isTableRow) {
                    paragraphs.add(new ExtractedText.Paragraph(index++, cells));
                } else {
                    paragraphs.add(new ExtractedText.Paragraph(index++, cells.get(0)));
                }
            }
        }

        return new ExtractedText(paragraphs);
    }

    private int extractX(String classAttr) {
        return Arrays.stream(classAttr.split(" "))
                .filter(cls -> cls.matches("x[0-9a-f]+"))
                .map(cls -> cls.substring(1))
                .mapToInt(s -> Integer.parseInt(s, 16))
                .findFirst()
                .orElse(0);
    }

    public ExtractedText mergeTranslation(ExtractedText original, TranslatedText translated) {
        List<ExtractedText.Paragraph> merged = new ArrayList<>();

        for (int i = 0; i < original.paragraphs.size(); i++) {
            ExtractedText.Paragraph originalPara = original.paragraphs.get(i);
            TranslatedText.Paragraph translatedPara = translated.paragraphs.get(i);

            if (originalPara.tableCells != null && !originalPara.tableCells.isEmpty()) {
                List<String> translatedCells = new ArrayList<>(originalPara.tableCells.size());

                for (int j = 0; j < originalPara.tableCells.size(); j++) {
                    String cellText = originalPara.tableCells.get(j);
                    if (shouldTranslate(cellText)) {
                        translatedCells.add(
                                translatedPara.texts != null && j < translatedPara.texts.size()
                                        ? translatedPara.texts.get(j)
                                        : cellText
                        );
                    } else {
                        translatedCells.add(cellText);
                    }
                }

                merged.add(new ExtractedText.Paragraph(originalPara.index, translatedCells));

            } else {
                String text = shouldTranslate(originalPara.text) ? translatedPara.text : originalPara.text;
                merged.add(new ExtractedText.Paragraph(originalPara.index, text));
            }
        }

        return new ExtractedText(merged);
    }


    private boolean shouldTranslate(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return false;
        if (trimmed.matches("[-–—•.0-9\\s]+")) return false;
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) return false;
        if (trimmed.matches("[0-9\\-\\–\\s]+")) return false;
        return true;
    }

}
