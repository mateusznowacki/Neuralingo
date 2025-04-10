package pl.pwr.Neuralingo.repository;



import  pl.pwr.Neuralingo.entity.TranslatedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TranslatedDocumentRepository extends MongoRepository<TranslatedDocument, String> {
}
