package pl.pwr.Neuralingo.translation.file.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfTranslatorTest {

    private PdfContentExtractor contentExtractor;
    private HtmlLayoutParser layoutParser;
    private HtmlTextReplacer textReplacer;
    private HtmlToPdfConverter pdfConverter;
    private AzureDocumentTranslationService azure;

    private PdfTranslator pdfTranslator;

    @BeforeEach
    void setUp() {
        contentExtractor = mock(PdfContentExtractor.class);
        layoutParser = mock(HtmlLayoutParser.class);
        textReplacer = mock(HtmlTextReplacer.class);
        pdfConverter = mock(HtmlToPdfConverter.class);
        azure = mock(AzureDocumentTranslationService.class);

        pdfTranslator = new PdfTranslator(contentExtractor, layoutParser, textReplacer, pdfConverter, azure);
    }

    @Test
    void testTranslatePdfDocument_successfulFlow() throws Exception {
        File inputPdf = new File("test.pdf");
        File outputPdf = new File("test_translated.pdf");

        String originalHtml = "<html>original</html>";
        String structuredHtml = "<html>structured</html>";

        ExtractedText extractedText = new ExtractedText(List.of(
                new Paragraph(0, "Hello World")
        ));

        TranslatedText translatedText = new TranslatedText(List.of(
                new Paragraph(0, "Witaj świecie")
        ));

        String finalHtml = "<html>translated</html>";

        when(contentExtractor.extractLayout(inputPdf)).thenReturn(originalHtml);
        when(layoutParser.buildStructuredHtml(originalHtml)).thenReturn(structuredHtml);
        when(contentExtractor.extractText(structuredHtml)).thenReturn(extractedText);
        when(azure.translate(extractedText, "pl")).thenReturn(translatedText);
        when(textReplacer.replaceText(structuredHtml, extractedText, translatedText)).thenReturn(finalHtml);
        when(pdfConverter.convertHtmlToPdf(finalHtml, outputPdf)).thenReturn(outputPdf);

        String resultPath = pdfTranslator.translatePdfDocument(inputPdf, "pl");

        assertEquals(outputPdf.getAbsolutePath(), resultPath);

        verify(contentExtractor).extractLayout(inputPdf);
        verify(layoutParser).buildStructuredHtml(originalHtml);
        verify(contentExtractor).extractText(structuredHtml);
        verify(azure).translate(extractedText, "pl");
        verify(textReplacer).replaceText(structuredHtml, extractedText, translatedText);
        verify(pdfConverter).convertHtmlToPdf(finalHtml, outputPdf);
    }

    @Test
    void testTranslatePdfDocument_interruptedException() throws Exception {
        File inputPdf = new File("broken.pdf");

        when(contentExtractor.extractLayout(inputPdf)).thenThrow(new InterruptedException("Simulated"));

        IOException exception = assertThrows(IOException.class, () ->
                pdfTranslator.translatePdfDocument(inputPdf, "en"));

        assertTrue(exception.getMessage().contains("Błąd konwersji PDF"));
    }
}
