package pl.pwr.Neuralingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.service.AzureBlobService;
import pl.pwr.Neuralingo.service.DocumentService;
import pl.pwr.Neuralingo.translation.DocumentTranslationFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RestController
@RequestMapping("/api/translate")
public class TranslatorController {

    private final DocumentTranslationFacade docTranslator;
    private final AzureBlobService azureBlobService;
    private final DocumentService documentService;

    @Autowired
    public TranslatorController(DocumentTranslationFacade docTranslator,
                                AzureBlobService azureBlobService,
                                DocumentService documentService) {
        this.docTranslator = docTranslator;
        this.azureBlobService = azureBlobService;
        this.documentService = documentService;
    }

    @PostMapping("/document/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> translateDocumentById(@PathVariable String id,
                                                             @RequestParam String targetLanguage,
                                                             Authentication auth) {
        Optional<DocumentEntity> docOpt = documentService.getEntityById(id, auth);
        if (docOpt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            DocumentEntity doc = docOpt.get();
            String extension = getExtension(doc.getFileType(), doc.getOriginalFilename());

            String rawPath = azureBlobService.downloadLocal(id);
            Path originalPath = Path.of(rawPath);
            Path renamedPath = Path.of(rawPath + "." + extension);
            Files.move(originalPath, renamedPath, StandardCopyOption.REPLACE_EXISTING);

            File inputFile = renamedPath.toFile();
            String translatedPath = docTranslator.translateFileDocument(inputFile, targetLanguage);

            File translatedFile = new File(translatedPath);
            String translatedBlobName = id + "_translated";
            String translatedBlobUrl = azureBlobService.uploadFile(translatedFile, translatedBlobName);

            doc.setTargetLanguage(targetLanguage);
            doc.setTranslatedFilename(translatedFile.getName());
            doc.setTranslatedStoragePath(translatedBlobUrl);
            documentService.updateDocument(doc);

            return ResponseEntity.ok(DocumentDTO.from(doc));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/document/ocr/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO> translateOcrDocumentById(@PathVariable String id,
                                                                @RequestParam String targetLanguage,
                                                                Authentication auth) {
        Optional<DocumentEntity> docOpt = documentService.getEntityById(id, auth);
        if (docOpt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            DocumentEntity doc = docOpt.get();
            String extension = getExtension(doc.getFileType(), doc.getOriginalFilename());

            String rawPath = azureBlobService.downloadLocal(id);
            Path originalPath = Path.of(rawPath);
            Path renamedPath = Path.of(rawPath + "." + extension);
            Files.move(originalPath, renamedPath, StandardCopyOption.REPLACE_EXISTING);

            File inputFile = renamedPath.toFile();
            String translatedPath = docTranslator.translateOcrDocument(inputFile, targetLanguage);

            File translatedFile = new File(translatedPath);
            String translatedBlobName = id + "_translated";
            String translatedBlobUrl = azureBlobService.uploadFile(translatedFile, translatedBlobName);

            doc.setTargetLanguage(targetLanguage);
            doc.setTranslatedFilename(translatedFile.getName());
            doc.setTranslatedStoragePath(translatedBlobUrl);
            documentService.updateDocument(doc);

            return ResponseEntity.ok(DocumentDTO.from(doc));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getExtension(String mime, String filename) {
        if (!"application/octet-stream".equals(mime)) {
            return getExtensionFromMime(mime);
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }

        return "bin";
    }


    private String getExtensionFromMime(String mime) {
        return switch (mime) {
            case "application/pdf" -> "pdf";
            case "application/msword" -> "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.ms-excel" -> "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "application/vnd.ms-powerpoint" -> "ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx";
            case "application/vnd.visio",
                 "application/vnd.ms-visio.drawing" -> "vsd";
            case "application/vnd.ms-visio.drawing.main+xml" -> "vsdx";
            case "text/plain" -> "txt";
            case "application/zip",
                 "application/x-zip-compressed" -> "zip";
            default -> "bin";
        };
    }

}
