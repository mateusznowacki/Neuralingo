package pl.pwr.Neuralingo.dto.document.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TableCellDto(
        String  content,
        int     rowIndex,
        int     columnIndex,

        /* te pola bywają opcjonalne → użyj Integer, aby null był OK */
        Integer rowSpan,
        Integer columnSpan,

        @JsonProperty("boundingRegions")
        BoundingRegionDto[] boundingRegions,
        SpanDto[] spans
) {}