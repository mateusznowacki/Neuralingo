package pl.pwr.Neuralingo.translation.pdf;


import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlTextReplacer {

    public String replaceText(String originalHtml, ExtractedText original, TranslatedText translated) {
        List<TranslatedText.Paragraph> translatedParagraphs = translated.paragraphs;

        StringBuilder result = new StringBuilder();
        Pattern divPattern = Pattern.compile("(<div class=\"t[^\"]*\">)(.*?)(</div>)", Pattern.DOTALL);
        Matcher matcher = divPattern.matcher(originalHtml);

        int paraIndex = 0;

        while (matcher.find()) {
            String openTag = matcher.group(1);
            String innerHtml = matcher.group(2);
            String closeTag = matcher.group(3);

            if (paraIndex >= translatedParagraphs.size()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(openTag + innerHtml + closeTag));
                continue;
            }

            String translatedText = translatedParagraphs.get(paraIndex).text;
            paraIndex++;

            // Zamień cały tekst w obrębie <div> na przetłumaczony, zachowując tagi
            String replacedContent = replaceVisibleText(innerHtml, translatedText);
            matcher.appendReplacement(result, Matcher.quoteReplacement(openTag + replacedContent + closeTag));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String replaceVisibleText(String htmlFragment, String newText) {
        // usuń wszystkie fragmenty tekstowe (czyli nie w <...>) i zostaw tylko strukturę
        // a potem wstaw newText w miejsce pierwszego tekstu
        Pattern partPattern = Pattern.compile("(<[^>]+>)|([^<]+)");
        Matcher matcher = partPattern.matcher(htmlFragment);

        StringBuilder rebuilt = new StringBuilder();
        boolean replaced = false;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // tag
                rebuilt.append(matcher.group(1));
            } else if (matcher.group(2) != null && !replaced) {
                // pierwszy tekst – zamień
                rebuilt.append(newText);
                replaced = true;
            }
            // inne teksty pomijamy – zastępujemy tylko cały widoczny tekst
        }

        return rebuilt.toString();
    }
}

