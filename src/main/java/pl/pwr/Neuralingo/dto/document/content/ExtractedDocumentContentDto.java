package pl.pwr.Neuralingo.dto.document.content;

public record ExtractedDocumentContentDto(
    ParagraphDto[] paragraphs,
    TableDto[] tables,
    ListItemDto[] listItems,
    KeyValuePairDto[] keyValuePairs,
    FigureDto[] figures
) {}
