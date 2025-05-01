package pl.pwr.Neuralingo.dto.document.content;

public record TableDto(
    int rowCount,
    int columnCount,
    BoundingRegionDto boundingRegion,
    SpanDto[] spans,
    TableCellDto[] cells
) {}
