package pl.pwr.Neuralingo.translation.file.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfContentExtractorTest {

    private PdfContentExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new PdfContentExtractor();
    }

    @Test
    void extractText_shouldExtractParagraphsCorrectly() {
        String html = """
            <html>
              <body>
                <div class="t1">Hello <span>world</span></div>
                <div class="t2">Another <b>line</b></div>
                <div class="t3"></div>
              </body>
            </html>
        """;

        ExtractedText result = extractor.extractText(html);

        List<Paragraph> paragraphs = result.getParagraphs();

        assertEquals(2, paragraphs.size());
        assertEquals("Hello world", paragraphs.get(0).getText());
        assertEquals("Another line", paragraphs.get(1).getText());
    }

    @Test
    void extractLayout_shouldReturnHtmlContent_whenPdf2HtmlSucceeds() throws Exception {
        // Setup
        File tempDir = Files.createTempDirectory("pdf-test").toFile();
        File fakePdf = new File(tempDir, "sample.pdf");
        File expectedHtml = new File(tempDir, "sample.html");

        // Create dummy HTML output
        String expectedContent = "<html><body>Test</body></html>";
        Files.writeString(expectedHtml.toPath(), expectedContent, StandardCharsets.UTF_8);

        // Fake input PDF file
        Files.writeString(fakePdf.toPath(), "fake-pdf", StandardCharsets.UTF_8);

        // Test
        String actualHtml;
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            actualHtml = extractor.extractLayout(fakePdf);
        }

        assertEquals(expectedContent, actualHtml);
    }
}
