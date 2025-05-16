package pl.pwr.Neuralingo.repository;



import org.springframework.data.mongodb.repository.MongoRepository;

public interface TranslatedDocumentRepository extends MongoRepository<TranslatedDocument, String> {
}
