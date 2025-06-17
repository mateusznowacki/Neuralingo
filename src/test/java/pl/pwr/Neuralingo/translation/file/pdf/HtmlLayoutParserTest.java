package pl.pwr.Neuralingo.translation.file.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HtmlLayoutParserTest {

    private HtmlLayoutParser parser;

    @BeforeEach
    void setUp() {
        parser = new HtmlLayoutParser();
    }

    @Test
    void buildStructuredHtml_shouldReturnUnchangedIfNoDiv() {
        String input = "<p>Some text without div</p>";
        String expected = input;  // No div with class="t..." so unchanged
        String actual = parser.buildStructuredHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    void buildStructuredHtml_shouldProcessSingleDivWithPlainText() {
        String input = "<div class=\"t1\">Hello world</div>";
        String expected = "<div class=\"t1\">Hello world</div>"; // No tags inside so unchanged
        String actual = parser.buildStructuredHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    void buildStructuredHtml_shouldProcessSingleDivWithMixedContent() {
        String input = "<div class=\"t3\">Hello <span>there</span> friend</div>";
        String expected = "<div class=\"t3\"><span></span>Hello there friend</div>";
        String actual = parser.buildStructuredHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    void buildStructuredHtml_shouldProcessMultipleDivs() {
        String input = "<div class=\"t1\"><b>Hello bold</b></div><div class=\"t2\"><i>Text italic</i></div>";
        String expected = "<div class=\"t1\"><b>Hello bold</b></div><div class=\"t2\"><i>Text italic</i></div>";
        String actual = parser.buildStructuredHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    void buildStructuredHtml_shouldHandleEmptyDiv() {
        String input = "<div class=\"t0\"></div>";
        String expected = "<div class=\"t0\"></div>";
        String actual = parser.buildStructuredHtml(input);
        assertEquals(expected, actual);
    }
}
