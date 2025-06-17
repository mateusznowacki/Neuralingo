package pl.pwr.Neuralingo.translation.file.pdf;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlTextReplacerTest {

    private final HtmlTextReplacer replacer = new HtmlTextReplacer();

    @Test
    void replaceText_replacesOriginalWithTranslated() {
        String html = "<p>Hello world!</p><p>Goodbye!</p>";
        ExtractedText original = new ExtractedText(List.of(
                new Paragraph(0, "Hello world!"),
                new Paragraph(1, "Goodbye!")
        ));
        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Bonjour le monde!"),
                new Paragraph(1, "Au revoir!")
        ));

        String result = replacer.replaceText(html, original, translated);

        assertTrue(result.contains("Bonjour le monde!"));
        assertTrue(result.contains("Au revoir!"));
        assertFalse(result.contains("Hello world!"));
        assertFalse(result.contains("Goodbye!"));
    }

    @Test
    void replaceText_doesNotReplaceWhenOriginalTextEmpty() {
        String html = "<p>Hello world!</p>";
        ExtractedText original = new ExtractedText(List.of(
                new Paragraph(0, "  ") // empty after trim
        ));
        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Bonjour")
        ));

        String result = replacer.replaceText(html, original, translated);

        // Nothing replaced
        assertEquals(html, result);
    }

    @Test
    void replaceText_ignoresParagraphsIfOriginalTextNotFoundInHtml() {
        String html = "<p>Hello world!</p>";
        ExtractedText original = new ExtractedText(List.of(
                new Paragraph(0, "Nonexistent text")
        ));
        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Quelque chose")
        ));

        String result = replacer.replaceText(html, original, translated);

        assertEquals(html, result);
    }

    @Test
    void replaceText_replacesOnlyFirstOccurrence() {
        String html = "<p>Hello world! Hello world!</p>";
        ExtractedText original = new ExtractedText(List.of(
                new Paragraph(0, "Hello world!")
        ));
        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Bonjour")
        ));

        String result = replacer.replaceText(html, original, translated);

        // Only first occurrence replaced
        assertTrue(result.contains("Bonjour"));
        assertTrue(result.contains("Hello world!"));
    }
}
