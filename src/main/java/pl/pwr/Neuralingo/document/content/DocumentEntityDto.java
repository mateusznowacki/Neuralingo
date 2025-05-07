package pl.pwr.Neuralingo.dto.document;

public record DocumentEntityDto(
        String role,
        int[] source,  // entity indexes
        int[] target   // entity indexes
) {}
