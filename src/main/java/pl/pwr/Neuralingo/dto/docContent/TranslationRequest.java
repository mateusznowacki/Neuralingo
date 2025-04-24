package pl.pwr.Neuralingo.dto.docContent;

public record TranslationRequest(
        String targetLanguage,
        String originalDocumentId
) {
}
