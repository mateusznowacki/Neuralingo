package pl.pwr.Neuralingo.translation.file.vsdx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VisioTranslatorTest {

    private VisioTextExtractor extractor;
    private VisioTextReplacer replacer;
    private AzureDocumentTranslationService azure;
    private VisioTranslator translator;

    @BeforeEach
    void setUp() {
        extractor = mock(VisioTextExtractor.class);
        replacer = mock(VisioTextReplacer.class);
        azure = mock(AzureDocumentTranslationService.class);
        translator = new VisioTranslator(extractor, replacer, azure);
    }

    @Test
    void translateVisioDocument_callsExtractorAndReplacerAndAzure() throws IOException {
        File inputFile = new File("input.vsdx");
        File outputFile = new File("output_translated.vsdx");

        ExtractedText extractedText = mock(ExtractedText.class);
        TranslatedText translatedText = mock(TranslatedText.class);

        when(extractor.extractText(inputFile)).thenReturn(extractedText);
        when(azure.translate(extractedText, "fr")).thenReturn(translatedText);
        when(replacer.replaceText(inputFile, extractedText, translatedText)).thenReturn(outputFile);

        String resultPath = translator.translateVisioDocument(inputFile, "fr");

        assertEquals(outputFile.getAbsolutePath(), resultPath);

        verify(extractor).extractText(inputFile);
        verify(azure).translate(extractedText, "fr");
        verify(replacer).replaceText(inputFile, extractedText, translatedText);
    }

    @Test
    void translateVisioDocument_propagatesIOException() throws IOException {
        File inputFile = new File("input.vsdx");
        when(extractor.extractText(inputFile)).thenThrow(new IOException("fail"));

        IOException thrown = assertThrows(IOException.class, () -> translator.translateVisioDocument(inputFile, "en"));
        assertEquals("fail", thrown.getMessage());

        verify(extractor).extractText(inputFile);
        verifyNoInteractions(azure, replacer);
    }
}
