package pl.pwr.Neuralingo.translation.pdf;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlLayoutParser {

    public String buildStructuredHtml(String html) {
        Pattern divPattern = Pattern.compile("(<div class=\"t[^\"]*\">)(.*?)(</div>)", Pattern.DOTALL);
        Matcher matcher = divPattern.matcher(html);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String open = matcher.group(1);
            String innerHtml = matcher.group(2);
            String close = matcher.group(3);

            String newContent = rebuildTextInDiv(innerHtml);
            matcher.appendReplacement(result, Matcher.quoteReplacement(open + newContent + close));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String rebuildTextInDiv(String innerHtml) {
        Pattern textPattern = Pattern.compile("([^<]+)|(<[^>]+>)");
        Matcher m = textPattern.matcher(innerHtml);

        List<String> textParts = new ArrayList<>();
        List<String> tokens = new ArrayList<>();

        while (m.find()) {
            if (m.group(1) != null) {
                // czysty tekst – wycinamy i zapamiętujemy
                textParts.add(m.group(1));
                tokens.add(""); // zostawiamy puste miejsce
            } else {
                tokens.add(m.group(2)); // tag – przepisujemy bez zmian
            }
        }

        // scalony tekst
        String combined = String.join("", textParts);

        // znajdź ostatni pusty wpis (ostatni tekst, który był usunięty)
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i).isEmpty()) {
                tokens.set(i, combined); // wstaw scalony tekst
                break;
            }
        }

        // złóż całość
        return String.join("", tokens);
    }
}
