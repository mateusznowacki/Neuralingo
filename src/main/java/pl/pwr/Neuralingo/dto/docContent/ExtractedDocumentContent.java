package pl.pwr.Neuralingo.dto.docContent;

import java.util.List;

public record ExtractedDocumentContent(
        String text,
        String language,
        List<Paragraph> paragraphs,
        List<Table> tables,
        List<StyleSpan> styles,
        List<DocumentSection> sections,
        List<KeyValuePair> keyValuePairs,
        List<NamedEntity> entities,
        List<Line> lines,
        List<Word> words
) {}

