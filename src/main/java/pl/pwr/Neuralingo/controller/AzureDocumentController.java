package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.docContent.ExtractedDocumentContent;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;
import pl.pwr.Neuralingo.service.DocumentService;

@RestController
@RequestMapping("/api/azure")
public class AzureDocumentController {

    @Autowired
    private AzureDocumentIntelligenceService azureDocumentIntelligenceService;

    @Autowired
    private DocumentService documentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/extract")
    public ResponseEntity<ExtractedDocumentContent> extractContent(@PathVariable String id,
                                                                   Authentication authentication) {
        // 1. Sprawdzenie dostępu do dokumentu
        OriginalDocument doc = documentService.getDocumentByIdAndUser(id, authentication.getName());

        // 2. Pobranie pliku z blob storage
        byte[] fileBytes = documentService.downloadFromBlob(doc.getStoragePath());

        // 3. Wysłanie do Azure Document Intelligence i zmapowanie wyniku
        ExtractedDocumentContent result = azureDocumentIntelligenceService.analyzeDocument(fileBytes, doc.getFileType());

        return ResponseEntity.ok(result);
    }

}
