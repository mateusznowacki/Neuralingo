package pl.pwr.Neuralingo.dto.document.content;

public record LanguageDto(
    String languageCode,
    SpanDto[] spans
) {}
