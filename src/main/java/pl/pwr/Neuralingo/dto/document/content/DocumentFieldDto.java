package pl.pwr.Neuralingo.dto.document.content;

public record DocumentFieldDto(
    String fieldName,
    String value,
    float confidence
) {}
