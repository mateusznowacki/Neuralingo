package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pwr.Neuralingo.service.AzureBlobService;
import pl.pwr.Neuralingo.translation.word.WordTranslationFacade;

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
    public String translateWord() {
        azureBlobService.downloadLocal("")

    }


}
