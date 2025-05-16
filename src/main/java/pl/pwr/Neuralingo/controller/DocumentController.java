package pl.pwr.Neuralingo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.service.AzureBlobService;
import pl.pwr.Neuralingo.service.DocumentService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AzureBlobService azureBlobService;

    // 1. Upload dokumentu
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return documentService.uploadDocument(file, authentication);
    }

    // 2. Pobierz wszystkie dokumenty użytkownika
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentDTO>> getAllUserDocuments(Authentication authentication) {
        return documentService.getAllUserDocuments(authentication);
    }

    // 3. Pobierz dokument po ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable String id, Authentication authentication) {
        return documentService.getDocumentById(id, authentication);
    }

    // 4. Usuń dokument po ID
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDocumentById(@PathVariable String id, Authentication authentication) {
        return documentService.deleteDocumentById(id, authentication);
    }

    // 5 pobierz dokument po ID
    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id, Authentication authentication) {
        Optional<DocumentEntity> docOpt = documentService.getEntityById(id, authentication);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        DocumentEntity doc = docOpt.get();
        byte[] content = azureBlobService.downloadFile(doc.getId());
        if (content == null) {
            return ResponseEntity.status(404).build();
        }
        String filename = doc.getOriginalFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}



