package pl.pwr.Neuralingo.dto.document.content;

public record KeyValuePairDto(
    KeyOrValueDto key,
    KeyOrValueDto value
) {}