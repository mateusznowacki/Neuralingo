package pl.pwr.Neuralingo.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AzureDocumentTranslationServiceTest {

    private AzureDocumentTranslationService service;

    @BeforeEach
    void setUp() {
        service = new AzureDocumentTranslationService();

        // Inject private fields
        ReflectionTestUtils.setField(service, "translatorEndpoint", "https://fake-translator.cognitiveservices.azure.com");
        ReflectionTestUtils.setField(service, "translatorApiKey", "fake-api-key");
        ReflectionTestUtils.setField(service, "translatorRegion", "fake-region");
    }

    @Test
    void testTranslate_Successful() throws Exception {
        // Prepare input: two paragraphs, one requires translation, one does not
        ExtractedText input = new ExtractedText(List.of(
                new Paragraph(0, "Hello world"),    // to be translated
                new Paragraph(1, "12345")            // should NOT be translated (numbers only)
        ));

        // Mock URL and HttpURLConnection to intercept the HTTP call inside translateBatch
        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConn = mock(HttpURLConnection.class);

        // Mock URL.openConnection() to return our mocked connection
        Mockito.when(mockUrl.openConnection()).thenReturn(mockConn);

        // We have to mock the constructor call new URL(...) inside translateBatch - tricky.
        // Solution: Use a helper method in service to inject URL or mock static URL constructor with tools like PowerMockito.
        // Since it's complex, we'll instead mock the URL class globally with PowerMockito or redesign code.
        // For simplicity here, we'll partially test translateBatch by making it package-private and call directly (or test translate indirectly).

        // Instead, we'll test translate() method but skip actual HTTP call by spying on service and mocking translateBatch()

        AzureDocumentTranslationService spyService = Mockito.spy(service);

        // Mock translateBatch to return a translated string for the text "Hello world"
        Mockito.doReturn(List.of("Cześć świecie")).when(spyService).translateBatch(List.of("Hello world"), "pl");

        // Call the public method translate with target language "pl"
        TranslatedText translatedText = spyService.translate(input, "pl");

        // Validate results
        List<Paragraph> paragraphs = translatedText.getParagraphs();

        // Paragraph with index 0 should be translated
        assertEquals("Cześć świecie", paragraphs.stream().filter(p -> p.getIndex() == 0).findFirst().get().getText());

        // Paragraph with index 1 should be unchanged (numbers only, no translation)
        assertEquals("12345", paragraphs.stream().filter(p -> p.getIndex() == 1).findFirst().get().getText());
    }
}
