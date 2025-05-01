package pl.pwr.Neuralingo.dto.document.content;

public record FigureDto(
    String description,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans
) {}
