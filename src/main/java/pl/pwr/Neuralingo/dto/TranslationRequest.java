package pl.pwr.Neuralingo.dto;

public record TranslationRequest(
        String targetLanguage,
        String originalDocumentId
) {
}
