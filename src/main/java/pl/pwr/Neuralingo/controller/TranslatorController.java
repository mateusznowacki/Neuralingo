package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pwr.Neuralingo.service.AzureBlobService;
import pl.pwr.Neuralingo.translation.word.WordTranslationFacade;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/translate")
public class TranslatorController {


    private final WordTranslationFacade word;
    private final AzureBlobService azureBlobService;

    @Autowired
    public TranslatorController(WordTranslationFacade word, AzureBlobService azureBlobService) {
        this.azureBlobService = azureBlobService;
        this.word = word;
    }











@GetMapping("/word")
public ResponseEntity<String> translateWord() {
    try {
        String localPath = azureBlobService.downloadLocal("6826a42ad621af7672f6ecc6");
        File inputFile = new File(localPath);
        String translatedPath = word.translateDocument(inputFile, "en");
        return ResponseEntity.ok("Przetłumaczony plik zapisano pod: " + translatedPath);
    } catch (Exception e) {
        e.printStackTrace(); // albo logger.error
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Błąd podczas tłumaczenia: " + e.getMessage());
    }
}

}
