package pl.pwr.Neuralingo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.entity.User;

import pl.pwr.Neuralingo.repository.DocumentRepository;
import pl.pwr.Neuralingo.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AzureBlobService azureBlobService;

    public ResponseEntity<DocumentEntity> uploadDocument(MultipartFile file, Authentication auth) {
        String userId = auth.getName();
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOpt.get();

        try {
            // 1. Najpierw utwórz obiekt i zapisz go, by mieć ID
            DocumentEntity doc = DocumentEntity.builder()
                    .originalFilename(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .sourceLanguage(user.getNativeLanguage())
                    .user(user)
                    .build();

            documentRepository.save(doc); // teraz doc.getId() jest dostępne

            // 2. Upload pliku z nazwą = ID
            String blobUrl = azureBlobService.uploadFile(file, doc.getId());

            // 3. Zaktualizuj ścieżkę i zapisz ponownie
            doc.setOriginalStoragePath(blobUrl);
            documentRepository.save(doc);

            return ResponseEntity.ok(doc);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<DocumentEntity>> getAllUserDocuments(Authentication auth) {
        String userId = auth.getName();
        List<DocumentEntity> docs = documentRepository.findByUserId(userId);
        return ResponseEntity.ok(docs);
    }

    public ResponseEntity<DocumentEntity> getDocumentById(String id, Authentication auth) {
        Optional<DocumentEntity> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty() || !docOpt.get().getUser().getId().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(docOpt.get());
    }

    public ResponseEntity<Void> deleteDocumentById(String id, Authentication auth) {
        Optional<DocumentEntity> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty() || !docOpt.get().getUser().getId().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        // Usuń z Azure Blob (nazwa blobu = ID dokumentu)
        azureBlobService.deleteFile(id);

        documentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
