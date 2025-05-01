package pl.pwr.Neuralingo.dto.document.content;

public record KeyOrValueDto(
    String content,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans
) {}