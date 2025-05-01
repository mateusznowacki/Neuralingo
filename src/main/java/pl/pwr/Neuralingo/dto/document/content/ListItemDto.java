package pl.pwr.Neuralingo.dto.document.content;

public record ListItemDto(
    String content,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans
) {}
