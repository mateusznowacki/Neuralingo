package pl.pwr.Neuralingo.dto.docContent;

public record Word(String content, int pageNumber, BoundingBox boundingBox, float confidence) {}
