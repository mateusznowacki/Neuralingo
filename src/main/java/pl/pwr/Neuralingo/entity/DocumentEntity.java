package pl.pwr.Neuralingo.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "documents")
@Builder
@Getter
@Setter
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

    private String ownerId;

}
