package pl.pwr.Neuralingo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.dto.UploadMetadata;
import pl.pwr.Neuralingo.entity.OriginalDocument;
import pl.pwr.Neuralingo.repository.OriginalDocumentRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private OriginalDocumentRepository originalRepo;

    @Autowired
    private ObjectMapper objectMapper;

    public OriginalDocument handleUpload(MultipartFile file, String metadataJson, String ownerId) {
        try {
            UploadMetadata metadata = objectMapper.readValue(metadataJson, UploadMetadata.class);
            String path = "src/main/resources/uploads/" + file.getOriginalFilename();

            File savedFile = new File(path);
            savedFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                fos.write(file.getBytes());
            }

            OriginalDocument doc = new OriginalDocument();
            doc.setTitle(metadata.title());
            doc.setSourceLanguage(metadata.sourceLanguage());
            doc.setOwnerId(ownerId);
            doc.setCreatedAt(LocalDateTime.now());
            doc.setStoragePath(path);
            doc.setFileType(file.getContentType());

            return originalRepo.save(doc);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisu dokumentu", e);
        }
    }

    public ResponseEntity<OriginalDocument> getOriginalDocument(String id) {
        return originalRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public List<OriginalDocument> getDocumentsByUser(String userId) {
        return originalRepo.findByOwnerId(userId);
    }
}
