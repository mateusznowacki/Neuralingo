package pl.pwr.Neuralingo.dto.document.content;

public record LineDto(
    String content,
    float[] polygon,
    SpanDto[] spans
) {}
