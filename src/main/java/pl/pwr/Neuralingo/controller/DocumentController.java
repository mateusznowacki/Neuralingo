package pl.pwr.Neuralingo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.dto.TranslationRequest;
import pl.pwr.Neuralingo.dto.UploadMetadata;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.entity.TranslatedDocument;
import pl.pwr.Neuralingo.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OriginalDocument> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") UploadMetadata metadata
    ) {
        OriginalDocument saved = documentService.uploadOriginal(file, metadata);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable String id) {
        return documentService.downloadOriginal(id);
    }

    @PostMapping("/translate")
    public ResponseEntity<?> translate(@RequestBody TranslationRequest request) {
        TranslatedDocument result = documentService.translateDocument(
                request.originalDocumentId(),
                request.targetLanguage()
        );
        return ResponseEntity.ok(result);
    }


}
