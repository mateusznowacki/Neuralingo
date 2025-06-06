package pl.pwr.Neuralingo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.service.AzureBlobService;
import pl.pwr.Neuralingo.service.DocumentService;
import pl.pwr.Neuralingo.translation.DocumentTranslationFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslatorControllerTest {

    @InjectMocks
    private TranslatorController translatorController;

    @Mock
    private DocumentTranslationFacade docTranslator;

    @Mock
    private AzureBlobService azureBlobService;

    @Mock
    private DocumentService documentService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private DocumentEntity createMockDocumentEntity() {
        DocumentEntity doc = mock(DocumentEntity.class);
        when(doc.getFileType()).thenReturn("text/plain");
        when(doc.getOriginalFilename()).thenReturn("file.txt");
        when(doc.getId()).thenReturn("doc1");
        return doc;
    }

    @Test
    void translateDocumentById_shouldReturnOk_whenSuccessful() throws IOException {
        DocumentEntity doc = createMockDocumentEntity();
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.of(doc));
        when(azureBlobService.downloadLocal("doc1")).thenReturn("path/to/file");
        // Mock Files.move() - no exception, so no need to stub

        File translatedFile = mock(File.class);
        when(translatedFile.getName()).thenReturn("file_translated.txt");
        when(docTranslator.translateFileDocument(any(File.class), eq("pl"))).thenReturn("path/to/translatedFile");

        File file = new File("path/to/translatedFile");
        // Mock azureBlobService.uploadFile
        when(azureBlobService.uploadFile(any(File.class), eq("doc1_translated"))).thenReturn("translated_blob_url");

        // Mock setters on DocumentEntity
        doNothing().when(doc).setTargetLanguage("pl");
        doNothing().when(doc).setTranslatedFilename("file_translated.txt");
        doNothing().when(doc).setTranslatedStoragePath("translated_blob_url");

        doNothing().when(documentService).updateDocument(doc);

        ResponseEntity<DocumentDTO> response = translatorController.translateDocumentById("doc1", "pl", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(documentService).updateDocument(doc);
    }

    @Test
    void translateDocumentById_shouldReturnForbidden_whenDocumentNotFound() {
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.empty());

        ResponseEntity<DocumentDTO> response = translatorController.translateDocumentById("doc1", "pl", authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void translateDocumentById_shouldReturnInternalServerError_onIOException() throws IOException {
        DocumentEntity doc = createMockDocumentEntity();
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.of(doc));
        when(azureBlobService.downloadLocal("doc1")).thenReturn("path/to/file");

        // Simulate Files.move throwing IOException
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.move(any(Path.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenThrow(new IOException());

            ResponseEntity<DocumentDTO> response = translatorController.translateDocumentById("doc1", "pl", authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Test
    void translateOcrDocumentById_shouldReturnOk_whenSuccessful() throws IOException {
        DocumentEntity doc = createMockDocumentEntity();
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.of(doc));
        when(azureBlobService.downloadLocal("doc1")).thenReturn("path/to/file");

        File translatedFile = mock(File.class);
        when(translatedFile.getName()).thenReturn("file_translated.txt");
        when(docTranslator.translateOcrDocument(any(File.class), eq("pl"))).thenReturn("path/to/translatedFile");

        when(azureBlobService.uploadFile(any(File.class), eq("doc1_translated"))).thenReturn("translated_blob_url");

        doNothing().when(doc).setTargetLanguage("pl");
        doNothing().when(doc).setTranslatedFilename("file_translated.txt");
        doNothing().when(doc).setTranslatedStoragePath("translated_blob_url");

        doNothing().when(documentService).updateDocument(doc);

        ResponseEntity<DocumentDTO> response = translatorController.translateOcrDocumentById("doc1", "pl", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(documentService).updateDocument(doc);
    }

    @Test
    void translateOcrDocumentById_shouldReturnForbidden_whenDocumentNotFound() {
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.empty());

        ResponseEntity<DocumentDTO> response = translatorController.translateOcrDocumentById("doc1", "pl", authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void translateOcrDocumentById_shouldReturnInternalServerError_onIOException() throws IOException {
        DocumentEntity doc = createMockDocumentEntity();
        when(documentService.getEntityById("doc1", authentication)).thenReturn(Optional.of(doc));
        when(azureBlobService.downloadLocal("doc1")).thenReturn("path/to/file");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.move(any(Path.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenThrow(new IOException());

            ResponseEntity<DocumentDTO> response = translatorController.translateOcrDocumentById("doc1", "pl", authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Test
    void getExtension_shouldReturnExtensionFromMime() {
        // Using reflection to invoke private method, or change method to package-private for testing
        // Here, we'll test getExtensionFromMime directly by reflection or by a small workaround

        // We will create a subclass for testing or make getExtension package-private
    }
}
