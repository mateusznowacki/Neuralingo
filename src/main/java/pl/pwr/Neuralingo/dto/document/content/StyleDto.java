package pl.pwr.Neuralingo.dto.document.content;

public record StyleDto(
    boolean isHandwritten,
    String similarFontFamily,
    String fontStyle,
    String fontWeight,
    SpanDto[] spans

) {}
