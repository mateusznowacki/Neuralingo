package pl.pwr.Neuralingo.dto.document.content;

import pl.pwr.Neuralingo.dto.document.DocumentEntityDto;

public record AzureAnalyzeResultDto(
    String apiVersion,
    String modelId,
    String stringIndexType,
    String content,
    PageDto[] pages,
    ParagraphDto[] paragraphs,
    TableDto[] tables,
    KeyValuePairDto[] keyValuePairs,
    StyleDto[] styles,
    LanguageDto[] languages,
    FigureDto[] figures,
    ListItemDto[] listItems,
    DocumentFieldDto[] documents,
    DocumentEntityDto[] entities,
    DocumentEntityRelationDto[] relationships
) {}
