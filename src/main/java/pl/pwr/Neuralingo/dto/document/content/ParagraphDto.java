package pl.pwr.Neuralingo.dto.document.content;

public record ParagraphDto(
    String content,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans,
    String role
) {}
