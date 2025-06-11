package pl.pwr.Neuralingo.translation.ocr;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class HtmltoDocxConverter {

    public File convertToWord(String translatedHtml, File outputFile) {
        try (XWPFDocument doc = new XWPFDocument()) {

            // Parsujemy HTML
            Document htmlDoc = Jsoup.parse(translatedHtml);

            // Obsługa nagłówków
            Elements headers = htmlDoc.select("h1, h2, h3");
            for (Element header : headers) {
                XWPFParagraph paragraph = doc.createParagraph();

                // Ustawiamy style nagłówków
                if (header.tagName().equalsIgnoreCase("h1")) {
                    paragraph.setStyle("Heading1");
                } else if (header.tagName().equalsIgnoreCase("h2")) {
                    paragraph.setStyle("Heading2");
                } else if (header.tagName().equalsIgnoreCase("h3")) {
                    paragraph.setStyle("Heading3");
                }

                XWPFRun run = paragraph.createRun();
                run.setText(header.text());
            }

            // Obsługa paragrafów
            Elements paragraphs = htmlDoc.select("p");
            for (Element p : paragraphs) {
                XWPFParagraph docxParagraph = doc.createParagraph();
                XWPFRun run = docxParagraph.createRun();
                run.setText(p.text());
            }

            // Obsługa list UL
            Elements unorderedLists = htmlDoc.select("ul");
            for (Element ul : unorderedLists) {
                Elements listItems = ul.select("li");
                for (Element li : listItems) {
                    XWPFParagraph paragraph = doc.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText("• " + li.text());
                }
            }

            // Obsługa list OL
            Elements orderedLists = htmlDoc.select("ol");
            for (Element ol : orderedLists) {
                Elements listItems = ol.select("li");
                int counter = 1;
                for (Element li : listItems) {
                    XWPFParagraph paragraph = doc.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(counter + ". " + li.text());
                    counter++;
                }
            }

            // Zapisujemy plik DOCX
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                doc.write(fos);
            }

            return outputFile;

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert HTML to DOCX: " + e.getMessage(), e);
        }
    }
}
