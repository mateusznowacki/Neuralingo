// === DocumentController.java ===
package pl.pwr.Neuralingo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.service.DocumentService;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class AzureDocumentController {

    @Autowired
    private DocumentService documentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OriginalDocument> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("metadata") String metadataJson,
            Authentication authentication
    ) throws JsonProcessingException {
        String ownerId = authentication.getName();
        OriginalDocument saved = documentService.handleUpload(file, metadataJson, ownerId);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<List<OriginalDocument>> getUserDocuments(Authentication authentication) {
        return ResponseEntity.ok(documentService.getDocumentsByUser(authentication.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id, Authentication authentication) {
        return documentService.downloadAndSaveLocally(id, authentication.getName());
    }
}

