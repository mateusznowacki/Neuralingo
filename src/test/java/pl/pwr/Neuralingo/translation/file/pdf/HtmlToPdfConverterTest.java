package pl.pwr.Neuralingo.translation.file.pdf;

import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlToPdfConverterTest {

    private HtmlToPdfConverter converter;

    @BeforeEach
    void setUp() {
        converter = new HtmlToPdfConverter();
    }

    @Test
    void convertHtmlToPdf_shouldCreatePdfFileSuccessfully() throws Exception {
        String htmlContent = "<html><body><h1>Hello</h1></body></html>";

        // Create dummy temp PDF file path
        File outputPdf = File.createTempFile("test-output", ".pdf");
        outputPdf.deleteOnExit();

        // Mock Process and behavior
        try (MockedConstruction<ProcessBuilder> mocked = mockConstruction(ProcessBuilder.class,
                (builder, context) -> {
                    Process mockProcess = mock(Process.class);
                    when(mockProcess.waitFor()).thenReturn(0);
                    when(mockProcess.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("PDF created".getBytes()));
                    when(builder.start()).thenReturn(mockProcess);
                })) {

            // Create a dummy PDF file (simulate Puppeteer output)
            File expectedPdf = new File(outputPdf.getAbsolutePath());
            Files.write(expectedPdf.toPath(), List.of("PDF DUMMY CONTENT"));

            File result = converter.convertHtmlToPdf(htmlContent, expectedPdf);

            assertNotNull(result);
            assertTrue(result.exists());
            assertTrue(result.getName().endsWith(".pdf"));
        }
    }

    @Test
    void convertHtmlToPdf_shouldThrowExceptionOnFailure() throws Exception {
        String htmlContent = "<html><body><h1>Error Case</h1></body></html>";
        File outputPdf = File.createTempFile("test-fail", ".pdf");
        outputPdf.deleteOnExit();

        try (MockedConstruction<ProcessBuilder> mocked = mockConstruction(ProcessBuilder.class,
                (builder, context) -> {
                    Process mockProcess = mock(Process.class);
                    when(mockProcess.waitFor()).thenReturn(1); // Simulate Puppeteer error
                    when(mockProcess.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("Error".getBytes()));
                    when(builder.start()).thenReturn(mockProcess);
                })) {

            IOException exception = assertThrows(IOException.class,
                    () -> converter.convertHtmlToPdf(htmlContent, outputPdf));

            assertTrue(exception.getMessage().contains("Puppeteer failed"));
        }
    }
}
