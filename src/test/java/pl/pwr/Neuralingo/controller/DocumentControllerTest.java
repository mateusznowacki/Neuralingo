//package pl.pwr.Neuralingo.controller;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.core.Authentication;
//import pl.pwr.Neuralingo.dto.DocumentDTO;
//import pl.pwr.Neuralingo.entity.DocumentEntity;
//import pl.pwr.Neuralingo.service.AzureBlobService;
//import pl.pwr.Neuralingo.service.DocumentService;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class DocumentControllerTest {
//
//    @InjectMocks
//    private DocumentController documentController;
//
//    @Mock
//    private DocumentService documentService;
//
//    @Mock
//    private AzureBlobService azureBlobService;
//
//    @Mock
//    private Authentication authentication;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    private DocumentDTO createMockDocumentDTO() {
//        return new DocumentDTO(
//                "1",
//                "user123",
//                "test.txt",
//                "text/plain",
//                "storage/original/test.txt",
//                "test_translated.txt",
//                "storage/translated/test_translated.txt",
//                "en",
//                "pl"
//        );
//    }
//
//    private DocumentEntity createMockDocumentEntity(String id) {
//        return new DocumentEntity(
//                id,
//                "user123",
//                "test.txt",
//                "text/plain",
//                "storage/original/test.txt",
//                "test_translated.txt",
//                "storage/translated/test_translated.txt",
//                "en",
//                "pl"
//        );
//    }
//
//    @Test
//    void uploadDocument_shouldReturnCreatedDocument() {
//        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
//        DocumentDTO documentDTO = createMockDocumentDTO();
//
//        when(documentService.uploadDocument(eq(file), any(Authentication.class)))
//                .thenReturn(ResponseEntity.ok(documentDTO));
//
//        ResponseEntity<DocumentDTO> response = documentController.uploadDocument(file, authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(documentDTO, response.getBody());
//    }
//
//    @Test
//    void getAllUserDocuments_shouldReturnDocumentList() {
//        List<DocumentDTO> docs = List.of(createMockDocumentDTO());
//        when(documentService.getAllUserDocuments(authentication)).thenReturn(ResponseEntity.ok(docs));
//
//        ResponseEntity<List<DocumentDTO>> response = documentController.getAllUserDocuments(authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(docs, response.getBody());
//    }
//
//    @Test
//    void getDocumentById_shouldReturnDocument() {
//        String docId = "1";
//        DocumentDTO dto = createMockDocumentDTO();
//
//        when(documentService.getDocumentById(docId, authentication)).thenReturn(ResponseEntity.ok(dto));
//
//        ResponseEntity<DocumentDTO> response = documentController.getDocumentById(docId, authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(dto, response.getBody());
//    }
//
//    @Test
//    void deleteDocumentById_shouldReturnOk() {
//        String docId = "1";
//        when(documentService.deleteDocumentById(docId, authentication)).thenReturn(ResponseEntity.ok().build());
//
//        ResponseEntity<Void> response = documentController.deleteDocumentById(docId, authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    void downloadDocument_shouldReturnFile() {
//        String docId = "1";
//        DocumentEntity doc = createMockDocumentEntity(docId);
//
//        when(documentService.getEntityById(docId, authentication)).thenReturn(Optional.of(doc));
//        when(azureBlobService.downloadFile(docId)).thenReturn("Hello".getBytes());
//
//        ResponseEntity<byte[]> response = documentController.downloadDocument(docId, authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Hello", new String(response.getBody()));
//        assertEquals("text/plain", response.getHeaders().getContentType().toString());
//    }
//
//    @Test
//    void downloadDocument_shouldReturn403IfNotFound() {
//        when(documentService.getEntityById("999", authentication)).thenReturn(Optional.empty());
//
//        ResponseEntity<byte[]> response = documentController.downloadDocument("999", authentication);
//
//        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//    }
//
//    @Test
//    void downloadDocument_shouldReturn404IfContentNull() {
//        String docId = "1";
//        DocumentEntity doc = createMockDocumentEntity(docId);
//
//        when(documentService.getEntityById(docId, authentication)).thenReturn(Optional.of(doc));
//        when(azureBlobService.downloadFile(docId)).thenReturn(null);
//
//        ResponseEntity<byte[]> response = documentController.downloadDocument(docId, authentication);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//    }
//
//    @Test
//    void downloadTranslatedDocument_shouldReturnTranslatedFile() {
//        String docId = "1";
//        DocumentEntity doc = createMockDocumentEntity(docId);
//
//        when(documentService.getEntityById(docId, authentication)).thenReturn(Optional.of(doc));
//        when(azureBlobService.downloadFile(docId + "_translated")).thenReturn("Translated".getBytes());
//
//        ResponseEntity<byte[]> response = documentController.downloadTranslatedDocument(docId, authentication);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Translated", new String(response.getBody()));
//    }
//
//    @Test
//    void downloadTranslatedDocument_shouldReturn404IfTranslatedNotFound() {
//        String docId = "1";
//        DocumentEntity doc = createMockDocumentEntity(docId);
//
//        when(documentService.getEntityById(docId, authentication)).thenReturn(Optional.of(doc));
//        when(azureBlobService.downloadFile(docId + "_translated")).thenReturn(null);
//
//        ResponseEntity<byte[]> response = documentController.downloadTranslatedDocument(docId, authentication);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//    }
//}
