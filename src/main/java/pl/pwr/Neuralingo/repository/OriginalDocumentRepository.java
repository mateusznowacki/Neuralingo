package pl.pwr.Neuralingo.repository;

import pl.pwr.Neuralingo.entity.OriginalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OriginalDocumentRepository extends MongoRepository<OriginalDocument, String> {
    List<OriginalDocument> findByOwnerId(String userId);

    List<OriginalDocument> findAllByOwnerId(String ownerId);
}
