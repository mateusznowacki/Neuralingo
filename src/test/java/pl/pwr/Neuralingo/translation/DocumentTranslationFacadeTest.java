package pl.pwr.Neuralingo.translation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.translation.file.pdf.PdfTranslator;
import pl.pwr.Neuralingo.translation.file.pptx.PptxTranslator;
import pl.pwr.Neuralingo.translation.file.vsdx.VisioTranslator;
import pl.pwr.Neuralingo.translation.file.word.WordTranslator;
import pl.pwr.Neuralingo.translation.file.xlsx.ExcelTranslator;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentTranslationFacadeTest {

    private WordTranslator wordTranslator;
    private PdfTranslator pdfTranslator;
    private ExcelTranslator excelTranslator;
    private PptxTranslator pptxTranslator;
    private VisioTranslator visioTranslator;

    private DocumentTranslationFacade facade;

    @BeforeEach
    void setUp() {
        wordTranslator = mock(WordTranslator.class);
        pdfTranslator = mock(PdfTranslator.class);
        excelTranslator = mock(ExcelTranslator.class);
        pptxTranslator = mock(PptxTranslator.class);
        visioTranslator = mock(VisioTranslator.class);

        facade = new DocumentTranslationFacade(wordTranslator, pdfTranslator, excelTranslator, pptxTranslator, visioTranslator);
    }

    @Test
    void translateFileDocument_shouldCallWordTranslator_forDocx() throws IOException {
        File file = mockFileWithName("file.docx");
        when(wordTranslator.translateWordDocument(file, "en")).thenReturn("translated-docx");

        String result = facade.translateFileDocument(file, "en");

        assertEquals("translated-docx", result);
        verify(wordTranslator).translateWordDocument(file, "en");
        verifyNoInteractions(pdfTranslator, excelTranslator, pptxTranslator, visioTranslator);
    }

    @Test
    void translateFileDocument_shouldCallPdfTranslator_forPdf() throws IOException {
        File file = mockFileWithName("file.pdf");
        when(pdfTranslator.translatePdfDocument(file, "en")).thenReturn("translated-pdf");

        String result = facade.translateFileDocument(file, "en");

        assertEquals("translated-pdf", result);
        verify(pdfTranslator).translatePdfDocument(file, "en");
        verifyNoInteractions(wordTranslator, excelTranslator, pptxTranslator, visioTranslator);
    }

    @Test
    void translateFileDocument_shouldCallPptxTranslator_forPptx() throws IOException {
        File file = mockFileWithName("file.pptx");
        when(pptxTranslator.translatePptxDocument(file, "en")).thenReturn("translated-pptx");

        String result = facade.translateFileDocument(file, "en");

        assertEquals("translated-pptx", result);
        verify(pptxTranslator).translatePptxDocument(file, "en");
        verifyNoInteractions(wordTranslator, pdfTranslator, excelTranslator, visioTranslator);
    }

    @Test
    void translateFileDocument_shouldCallExcelTranslator_forXlsx() throws IOException {
        File file = mockFileWithName("file.xlsx");
        when(excelTranslator.translateExcelDocument(file, "en")).thenReturn("translated-xlsx");

        String result = facade.translateFileDocument(file, "en");

        assertEquals("translated-xlsx", result);
        verify(excelTranslator).translateExcelDocument(file, "en");
        verifyNoInteractions(wordTranslator, pdfTranslator, pptxTranslator, visioTranslator);
    }

    @Test
    void translateFileDocument_shouldCallVisioTranslator_forVsdx() throws IOException {
        File file = mockFileWithName("file.vsdx");
        when(visioTranslator.translateVisioDocument(file, "en")).thenReturn("translated-vsdx");

        String result = facade.translateFileDocument(file, "en");

        assertEquals("translated-vsdx", result);
        verify(visioTranslator).translateVisioDocument(file, "en");
        verifyNoInteractions(wordTranslator, pdfTranslator, pptxTranslator, excelTranslator);
    }

    @Test
    void translateFileDocument_shouldThrowException_forUnsupportedExtension() {
        File file = mockFileWithName("file.txt");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> facade.translateFileDocument(file, "en"));

        assertTrue(exception.getMessage().contains("Nieobsługiwane rozszerzenie pliku"));
        verifyNoInteractions(wordTranslator, pdfTranslator, pptxTranslator, excelTranslator, visioTranslator);
    }

    @Test
    void translateOcrDocument_shouldBehaveSameAsTranslateFileDocument() throws IOException {
        File file = mockFileWithName("file.pdf");
        when(pdfTranslator.translatePdfDocument(file, "fr")).thenReturn("ocr-translated-pdf");

        String result = facade.translateOcrDocument(file, "fr");

        assertEquals("ocr-translated-pdf", result);
        verify(pdfTranslator).translatePdfDocument(file, "fr");
        verifyNoInteractions(wordTranslator, excelTranslator, pptxTranslator, visioTranslator);
    }

    // Helper method to create a mock File with a given name
    private File mockFileWithName(String name) {
        File file = mock(File.class);
        when(file.getName()).thenReturn(name);
        return file;
    }
}
