package pl.pwr.Neuralingo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pl.pwr.Neuralingo.dto.UploadMetadata;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.entity.TranslatedDocument;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;
import pl.pwr.Neuralingo.repository.TranslatedDocumentRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class DocumentService {

    @Autowired
    private OriginalDocumentRepository originalRepo;

    @Autowired
    private TranslatedDocumentRepository translatedRepo;

    public OriginalDocument uploadOriginal(MultipartFile file, UploadMetadata metadata) {
        try {
            // Ustal ścieżkę do folderu resources
            String folderPath = new File("src/main/resources/uploads").getAbsolutePath();

            // Upewnij się, że folder istnieje
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Zapisz plik do folderu
            String filePath = folderPath + File.separator + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            // Utwórz dokument
            OriginalDocument doc = new OriginalDocument();
            doc.setContent(new String(file.getBytes())); // opcjonalnie
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            doc.setOwnerId(metadata.ownerId());
            doc.setSourceLanguage(metadata.sourceLanguage());
            doc.setTitle(metadata.title());
            doc.setStoragePath(filePath); // dodaj to pole w klasie

            return originalRepo.save(doc);

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisu dokumentu", e);
        }
    }

    public ResponseEntity<?> downloadOriginal(String id) {
        return originalRepo.findById(id).map(doc -> {
            try {
                File file = new File(doc.getStoragePath());

                if (!file.exists()) {
                    return ResponseEntity.notFound().build();
                }

                byte[] fileBytes = Files.readAllBytes(file.toPath());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(doc.getFileType()))
                        .body(fileBytes);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Błąd podczas pobierania pliku");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    public TranslatedDocument translateDocument(String originalId, String targetLang) {
        OriginalDocument original = originalRepo.findById(originalId)
                .orElseThrow(() -> new RuntimeException("Dokument nie istnieje"));

        TranslatedDocument translated = new TranslatedDocument();
        translated.setOriginalDocumentId(original.getId());
        translated.setOwnerId(original.getOwnerId());
        translated.setTargetLanguage(targetLang);
        translated.setTranslatedContent("TRANSLATED: " + original.getContent()); // placeholder

        return translatedRepo.save(translated);
    }
}
