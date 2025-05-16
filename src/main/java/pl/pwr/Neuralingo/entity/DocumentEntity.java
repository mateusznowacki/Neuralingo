package pl.pwr.Neuralingo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id;

    private String fileType;
    private String originalFilename;
    private String originalStoragePath;

    private String translatedFilename;
    private String translatedFileType;
    private String translatedStoragePath;


    private String sourceLanguage;
    private String targetLanguage;

    @DBRef
    private User user;
}
