package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.service.DocumentService;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return documentService.uploadDocument(file, authentication);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentDTO>> getAllUserDocuments(Authentication authentication) {
        return documentService.getAllUserDocuments(authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable String id, Authentication authentication) {
        return documentService.getDocumentById(id, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDocumentById(@PathVariable String id, Authentication authentication) {
        return documentService.deleteDocumentById(id, authentication);
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id, Authentication authentication) {
        return documentService.downloadDocument(id, authentication);
    }

    @GetMapping("/download/translated/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadTranslatedDocument(@PathVariable String id, Authentication authentication) {
        return documentService.downloadTranslatedDocument(id, authentication);
    }
}
