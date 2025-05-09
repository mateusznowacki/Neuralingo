package pl.pwr.Neuralingo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class AzureResponseDto {

    public List<PageDto> pages;
    public List<ParagraphDto> paragraphs;
    public List<StyleDto> styles;
    public List<LanguageSpanDto> languages;
    public List<TableDto> tables;
    public List<FigureDto> figures;
    public List<SectionDto> sections;
    public List<KeyValuePairDto> keyValuePairs;
    public List<DocumentDto> documents;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageDto {
        public int pageNumber;
        public String unit;
        public double width;
        public double height;
        public double angle;
        public List<LineDto> lines;
        public List<WordDto> words;
        public List<SelectionMarkDto> selectionMarks;
        public List<BarcodeDto> barcodes;
        public List<SpanDto> spans;
    }

    public static class LineDto {
        public String content;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class WordDto {
        public String content;
        public double confidence;
        public BoundingRegion boundingRegion;
        public SpanDto span;
    }

    public static class SpanDto {
        public int offset;
        public int length;
    }

    public static class SelectionMarkDto {
        public String state;
        public double confidence;
        public BoundingRegion boundingRegion;
        public SpanDto span;
    }

    public static class BarcodeDto {
        public String kind;
        public String value;
        public double confidence;
        public BoundingRegion boundingRegion;
        public SpanDto span;
    }

    public static class ParagraphDto {
        public String role;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class StyleDto {
        public boolean isHandwritten;
        public double confidence;
        public List<SpanDto> spans;
    }

    public static class LanguageSpanDto {
        public String locale;
        public double confidence;
        public List<SpanDto> spans;
    }

    public static class TableDto {
        public int rowCount;
        public int columnCount;
        public List<TableCellDto> cells;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class TableCellDto {
        public int rowIndex;
        public int columnIndex;
        public int rowSpan;
        public int columnSpan;
        public String kind;
        public String content;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class FigureDto {
        public String caption;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class SectionDto {
        public String role;
        public List<Integer> childSections;
        public List<ParagraphDto> paragraphs;
        public List<TableDto> tables;
        public List<FigureDto> figures;
        public List<SpanDto> spans;
    }

    public static class KeyValuePairDto {
        public KeyValueElementDto key;
        public KeyValueElementDto value;
        public double confidence;
    }

    public static class KeyValueElementDto {
        public String content;
        public BoundingRegion boundingRegion;
        public List<SpanDto> spans;
    }

    public static class DocumentDto {
        public String docType;
        public double confidence;
        public List<BoundingRegion> boundingRegions;
        public List<SpanDto> spans;
        @JsonProperty("fields")
        public java.util.Map<String, FieldValueDto> fields;
    }

    public static class FieldValueDto {
        public String type;
        public Object value;  // może być String, number, list, map, etc.
        public double confidence;
        public BoundingRegion boundingRegion;
        public SpanDto span;
    }

    public static class BoundingRegion {
        public int pageNumber;
        public List<PointDto> polygon;
    }

    public static class PointDto {
        public double x;
        public double y;
    }
}
