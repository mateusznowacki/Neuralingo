package pl.pwr.Neuralingo.dto.document.content;

import pl.pwr.Neuralingo.dto.document.DocumentEntityDto;

public record ExtractedDocumentContentDto(
    ParagraphDto[] paragraphs,
    TableDto[] tables,
    ListItemDto[] listItems,
    KeyValuePairDto[] keyValuePairs,
    FigureDto[] figures,
    DocumentEntityDto[] entities,
    DocumentEntityRelationDto[] relationships
) {}
