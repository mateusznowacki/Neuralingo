package pl.pwr.Neuralingo.translation.file.pdf;


import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class HtmlTextReplacer {

    public String replaceText(String html, ExtractedText original, TranslatedText translated) {
        String updatedHtml = html;

        List<Paragraph> originalParagraphs = original.getParagraphs();
        List<Paragraph> translatedParagraphs = translated.getParagraphs();

        int size = Math.min(originalParagraphs.size(), translatedParagraphs.size());

        for (int i = 0; i < size; i++) {
            String originalText = originalParagraphs.get(i).getText().trim();
            String translatedText = translatedParagraphs.get(i).getText();

            if (!originalText.isEmpty() && updatedHtml.contains(originalText)) {
                updatedHtml = updatedHtml.replaceFirst(Pattern.quote(originalText), Matcher.quoteReplacement(translatedText));
            }
        }

        return updatedHtml;
    }





}

