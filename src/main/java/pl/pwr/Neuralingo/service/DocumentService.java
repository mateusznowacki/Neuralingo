
// === DocumentService.java ===
package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.pwr.Neuralingo.document.content.UploadMetadata;
import pl.pwr.Neuralingo.enums.DocumentStatus;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;
import pl.pwr.Neuralingo.utils.LocalSaver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final AzureBlobService azureBlobService;
    private final OriginalDocumentRepository originalRepo;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final LocalSaver localSaver;

    @Autowired
    public DocumentService(AzureBlobService azureBlobService,
                           OriginalDocumentRepository originalRepo,
                           ObjectMapper objectMapper,
                           UserService userService,
                           LocalSaver localSaver) {
        this.azureBlobService = azureBlobService;
        this.originalRepo = originalRepo;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.localSaver = localSaver;
    }

    public OriginalDocument handleUpload(MultipartFile file, String metadataJson, String ownerId) {
        try {
            UploadMetadata metadata = objectMapper.readValue(metadataJson, UploadMetadata.class);
            String title = metadata.title();
            String sourceLanguage = userService.getDefaultLanguageByUserId(ownerId);

            OriginalDocument document = new OriginalDocument();
            document.setTitle(title);
            document.setSourceLanguage(sourceLanguage);
            document.setOwnerId(ownerId);
            document.setCreatedAt(LocalDateTime.now());
            document.setFileType(file.getContentType());
            document.setArchived(false);
            document.setTranslated(false);
            document.setStatus(DocumentStatus.UPLOADED);
            document = originalRepo.save(document);

            String blobName = document.getId();
            azureBlobService.uploadFile(file, blobName);
            document.setStoragePath(blobName);

            return originalRepo.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Błąd przetwarzania uploadu", e);
        }
    }

    public OriginalDocument getDocumentByIdAndUser(String id, String ownerId) {
        return originalRepo.findById(id)
                .filter(doc -> doc.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Nie znaleziono dokumentu lub nie masz dostępu"));
    }

    public List<OriginalDocument> getDocumentsByUser(String ownerId) {
        return originalRepo.findAllByOwnerId(ownerId);
    }

    public ResponseEntity<byte[]> downloadAndSaveLocally(String id, String userId) {
        OriginalDocument doc = getDocumentByIdAndUser(id, userId);
        byte[] fileContent = azureBlobService.downloadFromBlob(doc.getStoragePath());
        localSaver.saveToLocalTempFile(fileContent, doc.getId(), doc.getFileType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getTitle() + "\"")
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(fileContent);
    }
}

