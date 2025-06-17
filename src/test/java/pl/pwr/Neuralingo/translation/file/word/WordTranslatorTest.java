package pl.pwr.Neuralingo.translation.file.word;

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

class WordTranslatorTest {

    WordTextExtractor extractor;
    WordTextReplacer replacer;
    AzureDocumentTranslationService azure;
    WordTranslator translator;

    @BeforeEach
    void setup() {
        extractor = mock(WordTextExtractor.class);
        replacer = mock(WordTextReplacer.class);
        azure = mock(AzureDocumentTranslationService.class);
        translator = new WordTranslator(extractor, replacer, azure);
    }

    @Test
    void translateWordDocument_successfulFlow() throws IOException {
        File inputFile = new File("test.docx");
        File expectedOutputFile = new File("test_translated.docx");

        ExtractedText extracted = new ExtractedText(List.of(
                new Paragraph(0, "Hello"),
                new Paragraph(1, "World")
        ));
        TranslatedText translated = new TranslatedText(List.of(
                new Paragraph(0, "Cześć"),
                new Paragraph(1, "Świat")
        ));

        // Mock extraction
        when(extractor.extractText(inputFile)).thenReturn(extracted);

        // Mock Azure translation
        when(azure.translate(extracted, "pl")).thenReturn(translated);

        // Mock replacer returns output file
        when(replacer.replaceText(inputFile, extracted, translated)).thenReturn(expectedOutputFile);

        String resultPath = translator.translateWordDocument(inputFile, "pl");

        // Verify interactions
        verify(extractor).extractText(inputFile);
        verify(azure).translate(extracted, "pl");
        verify(replacer).replaceText(inputFile, extracted, translated);

        // Verify returned path matches expected output file path
        assertEquals(expectedOutputFile.getAbsolutePath(), resultPath);
    }

    @Test
    void translateWordDocument_extractorThrowsIOException() throws IOException {
        File inputFile = new File("test.docx");

        when(extractor.extractText(inputFile)).thenThrow(new IOException("Failed to extract"));

        IOException thrown = assertThrows(IOException.class, () ->
                translator.translateWordDocument(inputFile, "pl")
        );

        assertTrue(thrown.getMessage().contains("Failed to extract"));
        verify(extractor).extractText(inputFile);
        verifyNoMoreInteractions(azure, replacer);
    }

    @Test
    void translateWordDocument_replacerThrowsIOException() throws IOException {
        File inputFile = new File("test.docx");
        ExtractedText extracted = new ExtractedText(List.of(new Paragraph(0, "Hello")));
        TranslatedText translated = new TranslatedText(List.of(new Paragraph(0, "Cześć")));

        when(extractor.extractText(inputFile)).thenReturn(extracted);
        when(azure.translate(extracted, "pl")).thenReturn(translated);
        when(replacer.replaceText(inputFile, extracted, translated)).thenThrow(new IOException("Failed to replace"));

        IOException thrown = assertThrows(IOException.class, () ->
                translator.translateWordDocument(inputFile, "pl")
        );

        assertTrue(thrown.getMessage().contains("Failed to replace"));
        verify(extractor).extractText(inputFile);
        verify(azure).translate(extracted, "pl");
        verify(replacer).replaceText(inputFile, extracted, translated);
    }
}
