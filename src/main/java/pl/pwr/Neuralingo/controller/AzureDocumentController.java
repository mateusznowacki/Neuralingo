package pl.pwr.Neuralingo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.document.content.ExtractedDocumentContentDto;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;
import pl.pwr.Neuralingo.service.DocumentService;
import pl.pwr.Neuralingo.dto.document.content.ExtractedDocumentContentDto;

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

    /**
     * OCR i ekstrakcja zawartości dokumentu
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/extract")
    public ResponseEntity<ExtractedDocumentContentDto> extractContent(@PathVariable String id,
                                                                      Authentication authentication) {
        logger.info("Rozpoczynam ekstrakcję dokumentu ID: {}", id);

        // 1️⃣ Sprawdzenie dostępu do dokumentu
        OriginalDocument doc = documentService.getDocumentByIdAndUser(id, authentication.getName());

        logger.info("Pobrano dokument: {} dla użytkownika: {}", doc.getTitle(), authentication.getName());

        // 2️⃣ Pobranie pliku z blob storage
        byte[] fileBytes = documentService.downloadFromBlob(doc.getStoragePath());

        logger.info("Pobrano plik ze ścieżki: {}", doc.getStoragePath());

        // 3️⃣ Wysłanie do Azure Document Intelligence i zmapowanie wyniku
        ExtractedDocumentContentDto result = azureDocumentIntelligenceService.analyzeDocument(fileBytes, doc.getFileType());

        logger.info("Zakończono ekstrakcję. Wykryto paragrafów: {}",
                    result.paragraphs() != null ? result.paragraphs().length : 0);

        return ResponseEntity.ok(result);
    }

//    /**
//     * OCR + tłumaczenie dokumentu na docelowy język
//     */
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/translate/{id}")
//    public ResponseEntity<ExtractedDocumentContentDto> translate(@PathVariable String id,
//                                                              @RequestParam(defaultValue = "en") String targetLang,
//                                                              Authentication auth) {
//        logger.info("Wywołano translate dla dokumentu ID: {}", id);
//        logger.info("Żądany język docelowy: {}", targetLang);
//
//        // 1️⃣ Sprawdzenie użytkownika z tokena
//        String username = auth != null ? auth.getName() : "brak użytkownika";
//        logger.info("Autoryzowany użytkownik: {}", username);
//
//        // 2️⃣ Weryfikacja dostępu do dokumentu
//        OriginalDocument doc = documentService.getDocumentByIdAndUser(id, username);
//        logger.info("Pobrano dokument: {}", doc.getFileName());
//
//        // 3️⃣ OCR / Extract
//        byte[] bytes = documentService.downloadFromBlob(doc.getStoragePath());
//        ExtractedDocumentContentDto ocr = azureDocumentIntelligenceService.analyzeDocument(bytes, doc.getFileType());
//
//        logger.info("OCR zakończony. Wykryto tekst o długości: {}",
//                    ocr.paragraphs() != null ? ocr.paragraphs() : 0);
//
//        // 4️⃣ Tłumaczenie
//        ExtractedDocumentContentDto translated = translationService.translate(ocr, targetLang);
//
//        logger.info("Tłumaczenie zakończone. Przetłumaczono {} paragrafów.",
//                    translated.paragraphs() != null ? translated.paragraphs().length : 0);
//
//        return ResponseEntity.ok(translated);
//    }
}
