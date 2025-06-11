package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.service.TranslationService;

@RestController
@RequestMapping("/api/translate")
public class TranslatorController {

    private final TranslationService translationService;

    @Autowired
    public TranslatorController(TranslationService translationService) {
        this.translationService = translationService;

    }

    @PostMapping("/document/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> translateDocumentById(@PathVariable String id,
                                                             @RequestParam String targetLanguage,
                                                             Authentication auth) {
        return translationService.handleTranslation(id, targetLanguage, false, auth);
    }

    @PostMapping("/document/ocr/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> translateOcrDocumentById(@PathVariable String id,
                                                                @RequestParam String targetLanguage,
                                                                Authentication auth) {
        return translationService.handleTranslation(id, targetLanguage, true, auth);
    }


}
