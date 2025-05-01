package pl.pwr.Neuralingo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "translated_documents")
public class TranslatedDocument {

    @Id
    private String id;

    private String translatedContent;
    private String ownerId;
    private String targetLanguage;

    private String originalDocumentId; // Referencja do OriginalDocument

    private Map<String, Object> formatting;

    private int wordCount;
    private int characterCount;

    private String translationProvider; // np. "OpenAI", "Google", "Human"

    private boolean reviewed;
    private String reviewerComment;

    private String fileName;
    private String fileType;

    private String storagePath;

    private boolean isArchived = false; // Domyślnie FALSE

    public TranslatedDocument() {
        this.reviewed = false;
        this.isArchived = false;
        this.translationProvider="Azure Translator";
    }

    // gettery i settery

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getOriginalDocumentId() {
        return originalDocumentId;
    }

    public void setOriginalDocumentId(String originalDocumentId) {
        this.originalDocumentId = originalDocumentId;
    }

    public Map<String, Object> getFormatting() {
        return formatting;
    }

    public void setFormatting(Map<String, Object> formatting) {
        this.formatting = formatting;
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

    public String getTranslationProvider() {
        return translationProvider;
    }

    public void setTranslationProvider(String translationProvider) {
        this.translationProvider = translationProvider;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getReviewerComment() {
        return reviewerComment;
    }

    public void setReviewerComment(String reviewerComment) {
        this.reviewerComment = reviewerComment;
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

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
