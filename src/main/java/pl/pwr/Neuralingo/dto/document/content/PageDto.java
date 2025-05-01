package pl.pwr.Neuralingo.dto.document.content;

public record PageDto(
    int pageNumber,
    float angle,
    float width,
    float height,
    String unit,
    WordDto[] words,
    LineDto[] lines,
    SelectionMarkDto[] selectionMarks,
    BarcodeDto[] barcodes
) {}




