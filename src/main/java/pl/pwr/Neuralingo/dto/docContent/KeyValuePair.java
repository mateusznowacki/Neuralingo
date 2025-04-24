package pl.pwr.Neuralingo.dto.docContent;

public record KeyValuePair(String key, String value, int pageNumber, BoundingBox keyBox, BoundingBox valueBox) {}
