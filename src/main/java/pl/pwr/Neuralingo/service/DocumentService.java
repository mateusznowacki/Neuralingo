package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.pwr.Neuralingo.dto.docContent.UploadMetadata;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final AzureBlobService azureBlobService;
    private final OriginalDocumentRepository originalRepo;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    public DocumentService(AzureBlobService azureBlobService,
                           OriginalDocumentRepository originalRepo,
                           ObjectMapper objectMapper,
                           UserService userService) {
        this.azureBlobService = azureBlobService;
        this.originalRepo = originalRepo;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    public OriginalDocument handleUpload(MultipartFile file, String metadataJson, String ownerId) {
        try {
            UploadMetadata metadata = objectMapper.readValue(metadataJson, UploadMetadata.class);
            String title = metadata.title();

            String sourceLanguage = userService.getDefaultLanguageByUserId(ownerId);

            // 1. Najpierw zapisujemy dokument do bazy, żeby dostać ID
            OriginalDocument document = new OriginalDocument();
            document.setTitle(title);
            document.setSourceLanguage(sourceLanguage);
            document.setOwnerId(ownerId);
            document.setCreatedAt(LocalDateTime.now());
            document.setFileType(file.getContentType());

            document = originalRepo.save(document); // teraz ma ID

            // 2. Używamy ID jako nazwy pliku
            String fileUrl = azureBlobService.uploadFile(file, document.getId()); // przesyłamy ID jako nazwa

            // 3. Uzupełniamy pole storagePath i ponownie zapisujemy
            document.setStoragePath(fileUrl);
            return originalRepo.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Błąd przetwarzania uploadu", e);
        }
    }


    public ResponseEntity<OriginalDocument> getOriginalDocument(String id) {
        return originalRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public OriginalDocument getDocumentByIdAndUser(String id, String ownerId) {
        return originalRepo.findById(id)
                .filter(doc -> doc.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono dokumentu lub nie masz dostępu"));
    }

    public List<OriginalDocument> getDocumentsByUser(String ownerId) {
        return originalRepo.findAllByOwnerId(ownerId);
    }

    public byte[] downloadFromBlob(String blobUrl) {
        String filename = extractFilenameFromUrl(blobUrl);
        return azureBlobService.downloadFile(filename);
    }

    private String extractFilenameFromUrl(String blobUrl) {
        return blobUrl.substring(blobUrl.lastIndexOf('/') + 1);
    }

}
