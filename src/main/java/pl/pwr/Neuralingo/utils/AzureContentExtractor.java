package pl.pwr.Neuralingo.utils;

import pl.pwr.Neuralingo.dto.document.content.*;

import pl.pwr.Neuralingo.dto.document.content.*;

import java.util.Arrays;

public class AzureContentExtractor {

    public ExtractedDocumentContentDto extractContent(AzureAnalyzeResultDto dto) {

        ParagraphDto[] paragraphs = dto.paragraphs();
        TableDto[] tables = dto.tables();
        ListItemDto[] listItems = dto.listItems();
        KeyValuePairDto[] keyValuePairs = dto.keyValuePairs();
        FigureDto[] figures = dto.figures();

        // Przypisanie stylów do paragrafów
        ParagraphDto[] paragraphsWithStyle = assignStylesToParagraphs(paragraphs, dto.styles());

        return new ExtractedDocumentContentDto(
                paragraphsWithStyle != null ? paragraphsWithStyle : new ParagraphDto[0],
                tables != null ? tables : new TableDto[0],
                listItems != null ? listItems : new ListItemDto[0],
                keyValuePairs != null ? keyValuePairs : new KeyValuePairDto[0],
                figures != null ? figures : new FigureDto[0]
        );
    }

    private ParagraphDto[] assignStylesToParagraphs(ParagraphDto[] paragraphs, StyleDto[] styles) {
        if (paragraphs == null || styles == null) return paragraphs;

        ParagraphDto[] updatedParagraphs = new ParagraphDto[paragraphs.length];

        for (int i = 0; i < paragraphs.length; i++) {
            ParagraphDto para = paragraphs[i];
            StyleDto matchedStyle = findStyleForParagraph(para, styles);
            // Tworzymy nowy ParagraphDto z przypisanym stylem
            updatedParagraphs[i] = new ParagraphDto(
                    para.content(),
                    para.boundingRegion(),
                    para.spans(),
                    para.role(),
                    matchedStyle // dodane style
            );
        }
        return updatedParagraphs;
    }

    private StyleDto findStyleForParagraph(ParagraphDto paragraph, StyleDto[] styles) {
        if (paragraph.spans() == null || paragraph.spans().length == 0) return null;

        int paragraphOffset = paragraph.spans()[0].offset();

        for (StyleDto style : styles) {
            if (style.spans() != null) {
                for (SpanDto span : style.spans()) {
                    int styleOffset = span.offset();
                    int styleEnd = span.offset() + span.length();
                    if (paragraphOffset >= styleOffset && paragraphOffset <= styleEnd) {
                        return style;
                    }
                }
            }
        }
        return null;
    }
}
