package pl.pwr.Neuralingo.entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "original_documents")
public class OriginalDocument extends BaseDocument {

    private String content;
    private String sourceLanguage;
    private String fileName;
    private String fileType;
    private String storagePath;

    private boolean isTranslated = false; // Domyślnie FALSE

    @DBRef(lazy = true)
    private List<TranslatedDocument> translations = new ArrayList<>();

    // gettery i settery

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public boolean isTranslated() {
        return isTranslated;
    }

    public void setTranslated(boolean translated) {
        isTranslated = translated;
    }

    public List<TranslatedDocument> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslatedDocument> translations) {
        this.translations = translations;
        this.isTranslated = (translations != null && !translations.isEmpty());
    }
}
