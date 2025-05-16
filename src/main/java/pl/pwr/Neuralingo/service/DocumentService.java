package pl.pwr.Neuralingo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;
import pl.pwr.Neuralingo.entity.User;
import pl.pwr.Neuralingo.repository.DocumentRepository;
import pl.pwr.Neuralingo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AzureBlobService azureBlobService;

    public ResponseEntity<DocumentDTO> uploadDocument(MultipartFile file, Authentication auth) {
        String userId = auth.getName();
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOpt.get();

        try {
            DocumentEntity doc = DocumentEntity.builder()
                    .originalFilename(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .sourceLanguage(user.getNativeLanguage())
                    .ownerId(user.getId())
                    .build();

            documentRepository.save(doc);

            String blobUrl = azureBlobService.uploadFile(file, doc.getId());

            doc.setOriginalStoragePath(blobUrl);
            documentRepository.save(doc);

            return ResponseEntity.ok(DocumentDTO.from(doc));

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<DocumentDTO>> getAllUserDocuments(Authentication auth) {
        String userId = auth.getName();
        List<DocumentEntity> docs = documentRepository.findByOwnerId(userId);
        List<DocumentDTO> result = docs.stream().map(DocumentDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<DocumentDTO> getDocumentById(String id, Authentication auth) {
        Optional<DocumentEntity> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty() || !docOpt.get().getOwnerId().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(DocumentDTO.from(docOpt.get()));
    }

    public ResponseEntity<Void> deleteDocumentById(String id, Authentication auth) {
        Optional<DocumentEntity> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty() || !docOpt.get().getOwnerId().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        azureBlobService.deleteFile(id);
        documentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public Optional<DocumentEntity> getEntityById(String id, Authentication authentication) {
        Optional<DocumentEntity> docOpt = documentRepository.findById(id);

        // Jeśli dokument nie istnieje lub nie należy do użytkownika – zwróć pusty Optional
        if (docOpt.isEmpty() || !docOpt.get().getOwnerId().equals(authentication.getName())) {
            return Optional.empty();
        }

        return docOpt;
    }

    public void updateDocument(DocumentEntity doc) {
        documentRepository.save(doc);
    }

}
