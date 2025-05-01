package pl.pwr.Neuralingo.dto.document.content;

public record BoundingRegionDto(
    int pageNumber,
    float[] polygon
) {}
