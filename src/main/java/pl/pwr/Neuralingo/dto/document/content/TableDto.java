package pl.pwr.Neuralingo.dto.document.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TableDto(
        @JsonProperty("rowCount")       int rowCount,
        @JsonProperty("columnCount")    int columnCount,

        /* Azure >= 2023-07-31 zwraca TABLICĘ regionów */
        @JsonProperty("boundingRegions")
        BoundingRegionDto[] boundingRegions,

        @JsonProperty("spans")          SpanDto[] spans,
        @JsonProperty("cells")          TableCellDto[] cells
) {}
