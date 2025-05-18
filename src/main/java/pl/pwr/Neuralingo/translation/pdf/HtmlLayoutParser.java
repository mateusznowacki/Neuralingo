package pl.pwr.Neuralingo.translation.pdf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class HtmlLayoutParser {

    public String buildStructuredHtml(String htmlFile) {
        Document doc = Jsoup.parse(htmlFile, "UTF-8");

        // Znajdujemy wszystkie <div class="t"> — zachowujemy strukturę
        Elements textElements = doc.select("div.t");

        for (Element t : textElements) {
            String fixedText = extractCleanText(t);
            t.empty(); // wyczyść wnętrze
            t.text(fixedText); // ustaw poprawny tekst
        }

        return doc.html();
    }

    private String extractCleanText(Element t) {
        StringBuilder merged = new StringBuilder();

        for (Node node : t.childNodes()) {
            if (node instanceof TextNode) {
                merged.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                Element span = (Element) node;

                // Pomijamy np. <span class="_ _0"> i podobne
                if (span.classNames().stream().anyMatch(cls -> cls.matches("_\\d*"))) {
                    continue;
                }

                // Rekurencyjnie scal tekst w spanach stylizacyjnych
                merged.append(extractCleanText(span));
            }
        }

        return merged.toString().trim();
    }
}
