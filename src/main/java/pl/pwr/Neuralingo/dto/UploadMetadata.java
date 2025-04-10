package pl.pwr.Neuralingo.dto;


public record UploadMetadata(
        String sourceLanguage,
        String ownerId,
        String title // możesz dodać też inne pola np. description
) {}
