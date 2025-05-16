package pl.pwr.Neuralingo.dto;

import pl.pwr.Neuralingo.entity.DocumentEntity;

public record DocumentDTO(
        String id,
        String ownerId,
        String originalFilename,
        String fileType,
        String originalStoragePath,
        String translatedFilename,
        String translatedStoragePath,
        String sourceLanguage,
        String targetLanguage
) {
    public static DocumentDTO from(DocumentEntity doc) {
        return new DocumentDTO(
                doc.getId(),
                doc.getOwnerId(),
                doc.getOriginalFilename(),
                doc.getFileType(),
                doc.getOriginalStoragePath(),
                doc.getTranslatedFilename(),
                doc.getTranslatedStoragePath(),
                doc.getSourceLanguage(),
                doc.getTargetLanguage()
        );
    }
}