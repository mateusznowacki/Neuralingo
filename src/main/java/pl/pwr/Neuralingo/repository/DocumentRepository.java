package pl.pwr.Neuralingo.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import pl.pwr.Neuralingo.entity.DocumentEntity;

import java.util.List;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {

    // Pobierz wszystkie dokumenty danego użytkownika po jego ID
    List<DocumentEntity> findByOwnerId(String userId);
}

