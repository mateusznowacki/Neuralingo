package pl.pwr.Neuralingo.dto.document.content;

public record DocumentEntityRelationDto(
        String role,
        String source,
        String target
) {}
