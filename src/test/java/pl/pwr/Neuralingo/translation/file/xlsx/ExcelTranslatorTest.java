package pl.pwr.Neuralingo.translation.file.xlsx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExcelTranslatorTest {

    ExcelTextExtractor extractor;
    ExcelTextReplacer replacer;
    AzureDocumentTranslationService azure;
    ExcelTranslator translator;

    @BeforeEach
    void setup() {
        extractor = mock(ExcelTextExtractor.class);
        replacer = mock(ExcelTextReplacer.class);
        azure = mock(AzureDocumentTranslationService.class);

        translator = new ExcelTranslator(extractor, replacer, azure);
    }

    @Test
    void translateExcelDocument_invokesDependenciesAndReturnsOutputPath() throws IOException {
        File inputFile = new File("input.xlsx");
        File outputFile = new File("input_translated.xlsx");

        ExtractedText extractedText = new ExtractedText(null);
        TranslatedText translatedText = new TranslatedText(null);

        when(extractor.extractText(inputFile)).thenReturn(extractedText);
        when(azure.translate(extractedText, "pl")).thenReturn(translatedText);
        when(replacer.replaceText(inputFile, extractedText, translatedText)).thenReturn(outputFile);

        String resultPath = translator.translateExcelDocument(inputFile, "pl");

        assertEquals(outputFile.getAbsolutePath(), resultPath);

        verify(extractor).extractText(inputFile);
        verify(azure).translate(extractedText, "pl");
        verify(replacer).replaceText(inputFile, extractedText, translatedText);
    }

    @Test
    void translateExcelDocument_propagatesIOException() throws IOException {
        File inputFile = new File("input.xlsx");
        when(extractor.extractText(inputFile)).thenThrow(new IOException("File error"));

        IOException exception = assertThrows(IOException.class, () -> translator.translateExcelDocument(inputFile, "pl"));

        assertEquals("File error", exception.getMessage());
    }
}
