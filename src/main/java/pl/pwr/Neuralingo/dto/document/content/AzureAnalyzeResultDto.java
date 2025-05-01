package pl.pwr.Neuralingo.dto.document.content;

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
    DocumentFieldDto[] documents
) {}
