package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.pwr.Neuralingo.document.content.UploadMetadata;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;
import pl.pwr.Neuralingo.enums.DocumentStatus;


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

            // 1️⃣ Pobierz domyślny język użytkownika (natywny)
            String sourceLanguage = userService.getDefaultLanguageByUserId(ownerId);

            // 2️⃣ Stwórz pusty dokument i ustaw podstawowe dane
            OriginalDocument document = new OriginalDocument();
            document.setTitle(title);
            document.setSourceLanguage(sourceLanguage);
            document.setOwnerId(ownerId);
            document.setCreatedAt(LocalDateTime.now());
            document.setFileType(file.getContentType());

            document.setArchived(false); // isArchived = false
            document.setTranslated(false); // isTranslated = false
            document.setStatus(DocumentStatus.UPLOADED);

            // 3️⃣ Zapisz dokument żeby dostać ID
            document = originalRepo.save(document);

            // 4️⃣ Użyj ID jako nazwy pliku i wgraj do blob storage
            String fileUrl = azureBlobService.uploadFile(file, document.getId()); // np. 60e3ab...pdf

            // 5️⃣ Zapisz ścieżkę do pliku
            document.setStoragePath(fileUrl);

            // 6️⃣ Finalne zapisanie dokumentu z storagePath
            return originalRepo.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Błąd przetwarzania uploadu", e);
        }
    }

    // Pobierz jeden dokument po ID
    public ResponseEntity<OriginalDocument> getOriginalDocument(String id) {
        return originalRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Pobierz dokument sprawdzając właściciela
    public OriginalDocument getDocumentByIdAndUser(String id, String ownerId) {
        return originalRepo.findById(id)
                .filter(doc -> doc.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono dokumentu lub nie masz dostępu"));
    }

    // Lista dokumentów użytkownika
    public List<OriginalDocument> getDocumentsByUser(String ownerId) {
        return originalRepo.findAllByOwnerId(ownerId);
    }

    // Pobieranie pliku z blob
    public byte[] downloadFromBlob(String blobUrl) {
        String filename = extractFilenameFromUrl(blobUrl);
        return azureBlobService.downloadFile(filename);
    }

    private String extractFilenameFromUrl(String blobUrl) {
        return blobUrl.substring(blobUrl.lastIndexOf('/') + 1);
    }
}
