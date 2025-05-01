package pl.pwr.Neuralingo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.docContent.ExtractedDocumentContent;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.service.DocumentService;

@RestController
@RequestMapping("/api/azure")
public class AzureDocumentController {

    private final DocumentService documentService;
    private final AzureDocumentIntelligenceService azureDocumentIntelligenceService;
    private final AzureDocumentTranslationService translationService;
 private static final Logger logger = LoggerFactory.getLogger(AzureDocumentController.class);

    public AzureDocumentController(DocumentService documentService,
                                   AzureDocumentIntelligenceService azureDocumentIntelligenceService,
                                   AzureDocumentTranslationService translationService) {
        this.documentService = documentService;
        this.azureDocumentIntelligenceService = azureDocumentIntelligenceService;
        this.translationService = translationService;
    }

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

  @PreAuthorize("isAuthenticated()")
    @PostMapping("/translate/{id}")
    public ResponseEntity<ExtractedDocumentContent> translate(@PathVariable String id,
                                                              @RequestParam(defaultValue = "en") String targetLang,
                                                              Authentication auth) {
        logger.info("Wywołano translate dla dokumentu ID: {}", id);
        logger.info("Żądany język docelowy: {}", targetLang);

        // sprawdzenie użytkownika z tokena
        String username = auth != null ? auth.getName() : "brak użytkownika";
        logger.info("Autoryzowany użytkownik: {}", username);

        // 1. weryfikacja dostępu
        OriginalDocument doc = documentService.getDocumentByIdAndUser(id, username);
        logger.info("Pobrano dokument: {}", doc.getFileName());

        // 2. OCR
        byte[] bytes = documentService.downloadFromBlob(doc.getStoragePath());
        ExtractedDocumentContent ocr = azureDocumentIntelligenceService.analyzeDocument(bytes, doc.getFileType());

        logger.info("OCR zakończony. Wykryto tekst o długości: {}", ocr.text() != null ? ocr.text().length() : 0);

        // 3. tłumaczenie
        ExtractedDocumentContent translated = translationService.translate(ocr, targetLang);

        logger.info("Tłumaczenie zakończone.");

        return ResponseEntity.ok(translated);
    }


}
