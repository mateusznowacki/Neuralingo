package pl.pwr.Neuralingo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.entity.User;
import pl.pwr.Neuralingo.repository.DocumentRepository;
import pl.pwr.Neuralingo.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentServiceTest {

    private DocumentRepository documentRepository;
    private UserRepository userRepository;
    private AzureBlobService azureBlobService;
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        userRepository = mock(UserRepository.class);
        azureBlobService = mock(AzureBlobService.class);

        documentService = new DocumentService();
        injectPrivateField(documentService, "documentRepository", documentRepository);
        injectPrivateField(documentService, "userRepository", userRepository);
        injectPrivateField(documentService, "azureBlobService", azureBlobService);
    }

    @Test
    void uploadDocument_shouldUploadAndReturnDocumentDTO() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        User user = new User();
        user.setId("user1");
        user.setNativeLanguage("en");

        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getContentType()).thenReturn("application/pdf");

        when(documentRepository.save(any())).thenAnswer(invocation -> {
            DocumentEntity saved = invocation.getArgument(0);
            saved.setId("docId");
            return saved;
        });

        when(azureBlobService.uploadFile(file, "docId")).thenReturn("https://blob.url/docId");

        ResponseEntity<DocumentDTO> response = documentService.uploadDocument(file, auth);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("doc.pdf", response.getBody().originalFilename());
        assertEquals("https://blob.url/docId", response.getBody().originalStoragePath());
    }

    @Test
    void getAllUserDocuments_shouldReturnDocumentList() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        DocumentEntity doc = DocumentEntity.builder()
                .id("doc1")
                .ownerId("user1")
                .originalFilename("doc.pdf")
                .fileType("application/pdf")
                .originalStoragePath("https://blob.url/doc1")
                .sourceLanguage("en")
                .build();

        when(documentRepository.findByOwnerId("user1")).thenReturn(List.of(doc));

        ResponseEntity<List<DocumentDTO>> response = documentService.getAllUserDocuments(auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("doc1", response.getBody().get(0).id());
    }

    @Test
    void getDocumentById_shouldReturnDocumentIfOwnedByUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        DocumentEntity doc = DocumentEntity.builder()
                .id("doc1")
                .ownerId("user1")
                .originalFilename("doc.pdf")
                .fileType("application/pdf")
                .originalStoragePath("https://blob.url/doc1")
                .sourceLanguage("en")
                .build();

        when(documentRepository.findById("doc1")).thenReturn(Optional.of(doc));

        ResponseEntity<DocumentDTO> response = documentService.getDocumentById("doc1", auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("doc1", response.getBody().id());
    }

    @Test
    void getDocumentById_shouldReturn403IfNotOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user2");

        DocumentEntity doc = DocumentEntity.builder()
                .id("doc1")
                .ownerId("user1")
                .build();

        when(documentRepository.findById("doc1")).thenReturn(Optional.of(doc));

        ResponseEntity<DocumentDTO> response = documentService.getDocumentById("doc1", auth);

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void deleteDocumentById_shouldDeleteIfOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        DocumentEntity doc = DocumentEntity.builder()
                .id("doc1")
                .ownerId("user1")
                .build();

        when(documentRepository.findById("doc1")).thenReturn(Optional.of(doc));

        ResponseEntity<Void> response = documentService.deleteDocumentById("doc1", auth);

        assertEquals(200, response.getStatusCodeValue());
        verify(azureBlobService).deleteFile("doc1");
        verify(documentRepository).deleteById("doc1");
    }

    @Test
    void deleteDocumentById_shouldReturn403IfNotOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user2");

        DocumentEntity doc = DocumentEntity.builder()
                .id("doc1")
                .ownerId("user1")
                .build();

        when(documentRepository.findById("doc1")).thenReturn(Optional.of(doc));

        ResponseEntity<Void> response = documentService.deleteDocumentById("doc1", auth);

        assertEquals(403, response.getStatusCodeValue());
        verify(azureBlobService, never()).deleteFile(anyString());
        verify(documentRepository, never()).deleteById(anyString());
    }

    private void injectPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
