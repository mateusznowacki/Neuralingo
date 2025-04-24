package pl.pwr.Neuralingo.dto.docContent;


public record UploadMetadata(
        String sourceLanguage,
        String ownerId,
        String title // możesz dodać też inne pola np. description
) {}
