package pl.pwr.Neuralingo.service;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AzureDocumentIntelligenceServiceTest {

    private AzureDocumentIntelligenceService service;
    private HttpClient mockClient;

    @BeforeEach
    void setUp() {
        service = new AzureDocumentIntelligenceService();

        // Inject values
        ReflectionTestUtils.setField(service, "endpoint", "https://mock-endpoint.com");
        ReflectionTestUtils.setField(service, "apiKey", "fake-api-key");

        // Mock HttpClient and inject
        mockClient = Mockito.mock(HttpClient.class);
        ReflectionTestUtils.setField(service, "client", mockClient);
    }

    @Test
    void testAnalyzeAndSaveOnly_Successful() throws Exception {

    }

    @Test
    void testAnalyzeAndSaveOnly_ThrowsExceptionOnError() throws Exception {
        byte[] fileBytes = "bad content".getBytes();
        String fileType = "application/pdf";

        HttpResponse<String> submitResponse = Mockito.mock(HttpResponse.class);
        when(submitResponse.statusCode()).thenReturn(500);
        when(submitResponse.body()).thenReturn("Error");

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(submitResponse);

        assertThrows(RuntimeException.class, () -> {
            service.analyzeAndSaveOnly(fileBytes, fileType);
        });
    }
}
