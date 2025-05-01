package pl.pwr.Neuralingo.dto.document.content;

public record WordDto(
    String content,
    float[] polygon,
    float confidence,
    SpanDto span
) {}
