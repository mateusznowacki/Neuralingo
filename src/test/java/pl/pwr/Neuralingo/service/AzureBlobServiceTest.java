package pl.pwr.Neuralingo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AzureBlobServiceTest {

    private BlobContainerClient containerClient;
    private BlobClient blobClient;
    private AzureBlobService azureBlobService;

    @BeforeEach
    void setup() {
        containerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);

        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(containerClient.exists()).thenReturn(true);
        when(blobClient.getBlobUrl()).thenReturn("https://fake.blob.url/test");

        // Create a test-specific subclass to inject mocked containerClient
        azureBlobService = new AzureBlobService(containerClient);
    }

    @Test
    void testUploadFile() throws IOException {
        File file = File.createTempFile("test", ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Hello Blob");
        }

        String result = azureBlobService.uploadFile(file, "testBlob");

        assertEquals("https://fake.blob.url/test", result);
        verify(blobClient, times(1)).upload(any(InputStream.class), eq(file.length()), eq(true));

        // Clean up
        file.deleteOnExit();
    }

    @Test
    void testDownloadFile() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(0);
            os.write("test content".getBytes());
            return null;
        }).when(blobClient).downloadStream(any(OutputStream.class));

        when(blobClient.exists()).thenReturn(true);

        byte[] data = azureBlobService.downloadFile("testBlob");
        assertNotNull(data);
        assertEquals("test content", new String(data));
    }

    @Test
    void testDeleteFile() {
        when(blobClient.exists()).thenReturn(true);

        azureBlobService.deleteFile("testBlob");

        verify(blobClient, times(1)).delete();
    }
}
