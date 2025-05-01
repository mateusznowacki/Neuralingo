package pl.pwr.Neuralingo.dto.document.content;

public record TableCellDto(
    int rowIndex,
    int columnIndex,
    String content,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans
) {}