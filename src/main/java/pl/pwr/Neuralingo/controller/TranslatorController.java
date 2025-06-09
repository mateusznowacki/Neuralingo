package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.service.TranslationService;
import pl.pwr.Neuralingo.translation.ocr.OcrTranslator;

import java.io.File;

@RestController
@RequestMapping("/api/translate")
public class TranslatorController {

    private final TranslationService translationService;

    private final OcrTranslator ocrTranslator;

    @Autowired
    public TranslatorController(TranslationService translationService, OcrTranslator ocrTranslator) {
        this.translationService = translationService;
        this.ocrTranslator = ocrTranslator;
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

    @PostMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> testOcr(@RequestParam String targetLanguage, Authentication auth) {
        try {
            File inputFile = new File("/home/mateusz-nowacki/Documents/Tekst, obrazek, tabelka - Procedura diagnostyki i naprawy układu hamulcoweg2.pdf");
            String html = ocrTranslator.translateDocument(inputFile, targetLanguage);
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Translation failed: " + e.getMessage());
        }
    }



}
