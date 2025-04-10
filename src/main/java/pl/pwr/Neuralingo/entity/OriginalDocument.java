package pl.pwr.Neuralingo.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "original_documents")
public class OriginalDocument extends BaseDocument{

    private String content;

    private String sourceLanguage;

    private String detectedLanguage;

    private int wordCount;

    private int characterCount;

    private String fileName;

    private String fileType;

    private Map<String, Object> formatting;

    private String storagePath; // <- nowo dodane

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public OriginalDocument() {
        super();
    }

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

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
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

    public Map<String, Object> getFormatting() {
        return formatting;
    }

    public void setFormatting(Map<String, Object> formatting) {
        this.formatting = formatting;
    }
}
