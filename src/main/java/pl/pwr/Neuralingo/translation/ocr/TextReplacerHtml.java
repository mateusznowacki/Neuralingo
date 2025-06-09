package pl.pwr.Neuralingo.translation.ocr;

import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

@Component
public class TextReplacerHtml {

    public String replaceTextInHtml(String html, ExtractedText extractedText, TranslatedText translatedText) {

        if (extractedText.getParagraphs().size() != translatedText.getParagraphs().size()) {
            throw new IllegalArgumentException("ExtractedText and TranslatedText have different number of paragraphs!");
        }

        String modifiedHtml = html;

        for (int i = 0; i < extractedText.getParagraphs().size(); i++) {
            Paragraph extractedParagraph = extractedText.getParagraphs().get(i);
            Paragraph translatedParagraph = translatedText.getParagraphs().get(i);

            String originalText = extractedParagraph.getText().trim();
            String newText = translatedParagraph.getText().trim();

            // Podmieniamy w <div class="text">
            modifiedHtml = replaceInDiv(modifiedHtml, "text", originalText, newText);

            // Podmieniamy w <div class="cell">
            modifiedHtml = replaceInDiv(modifiedHtml, "cell", originalText, newText);
        }

        return modifiedHtml;
    }

    private String replaceInDiv(String html, String divClass, String originalText, String newText) {
        StringBuilder result = new StringBuilder();

        int lastIndex = 0;
        while (true) {
            // Znajdź początek div-a
            String divStart = "<div class=\"" + divClass + "\"";
            int divOpenIndex = html.indexOf(divStart, lastIndex);
            if (divOpenIndex == -1) {
                break;
            }

            // Znajdź koniec otwierającego taga '>'
            int contentStartIndex = html.indexOf('>', divOpenIndex);
            if (contentStartIndex == -1) {
                break;
            }
            contentStartIndex += 1; // przechodzimy za '>'

            // Znajdź zamknięcie div-a
            int divCloseIndex = html.indexOf("</div>", contentStartIndex);
            if (divCloseIndex == -1) {
                break;
            }

            // Wyciągnij aktualny tekst wewnętrzny
            String currentText = html.substring(contentStartIndex, divCloseIndex).trim();

            // Dodaj kawałek HTML bez zmian
            result.append(html, lastIndex, contentStartIndex);

            // Jeśli pasuje, to podmień tekst
            if (currentText.equals(originalText)) {
                result.append(newText);
            } else {
                result.append(currentText);
            }

            // Dodaj zamknięcie </div>
            result.append("</div>");

            // Przesuń lastIndex do następnego fragmentu
            lastIndex = divCloseIndex + "</div>".length();
        }

        // Dodaj resztę HTML-a po ostatnim div-ie
        result.append(html.substring(lastIndex));

        return result.toString();
    }
}
