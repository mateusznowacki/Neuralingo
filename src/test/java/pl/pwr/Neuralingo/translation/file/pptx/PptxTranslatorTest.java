package pl.pwr.Neuralingo.translation.file.pptx;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PptxTranslatorTest {

    @Test
    void translatePptxDocument_shouldReturnTranslatedFilePath() throws IOException {
        // Arrange
        PptxTextExtractor extractor = mock(PptxTextExtractor.class);
        PptxTextReplacer replacer = mock(PptxTextReplacer.class);
        AzureDocumentTranslationService azure = mock(AzureDocumentTranslationService.class);

        File inputFile = new File("input.pptx");
        File outputFile = new File("input_translated.pptx");

        ExtractedText extractedText = mock(ExtractedText.class);
        TranslatedText translatedText = mock(TranslatedText.class);

        when(extractor.extractText(inputFile)).thenReturn(extractedText);
        when(azure.translate(extractedText, "fr")).thenReturn(translatedText);
        when(replacer.replaceText(inputFile, extractedText, translatedText)).thenReturn(outputFile);

        PptxTranslator translator = new PptxTranslator(extractor, replacer, azure);

        // Act
        String resultPath = translator.translatePptxDocument(inputFile, "fr");

        // Assert
        assertEquals(outputFile.getAbsolutePath(), resultPath);

        verify(extractor).extractText(inputFile);
        verify(azure).translate(extractedText, "fr");
        verify(replacer).replaceText(inputFile, extractedText, translatedText);
    }
}
