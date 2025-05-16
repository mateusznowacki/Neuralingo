package pl.pwr.Neuralingo.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "documents")
@Builder
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

    private String userId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getOriginalStoragePath() {
        return originalStoragePath;
    }

    public void setOriginalStoragePath(String originalStoragePath) {
        this.originalStoragePath = originalStoragePath;
    }

    public String getTranslatedFilename() {
        return translatedFilename;
    }

    public void setTranslatedFilename(String translatedFilename) {
        this.translatedFilename = translatedFilename;
    }

    public String getTranslatedFileType() {
        return translatedFileType;
    }

    public void setTranslatedFileType(String translatedFileType) {
        this.translatedFileType = translatedFileType;
    }

    public String getTranslatedStoragePath() {
        return translatedStoragePath;
    }

    public void setTranslatedStoragePath(String translatedStoragePath) {
        this.translatedStoragePath = translatedStoragePath;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
