package pl.pwr.Neuralingo.repository;

import pl.pwr.Neuralingo.entity.OriginalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OriginalDocumentRepository extends MongoRepository<OriginalDocument, String> {
}
