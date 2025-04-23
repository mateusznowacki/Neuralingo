package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.service.AzureDocumentService;

@RestController
@RequestMapping("/api/azure")
public class AzureDocumentController {

    @Autowired
    private AzureDocumentService azureDocumentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/translate/{id}")
    public ResponseEntity<String> analyzeAndTranslate(
            @PathVariable String id,
            @RequestParam String targetLang
    ) {
        try {
            String translated = azureDocumentService.analyzeAndTranslate(id, targetLang);
            return ResponseEntity.ok(translated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
