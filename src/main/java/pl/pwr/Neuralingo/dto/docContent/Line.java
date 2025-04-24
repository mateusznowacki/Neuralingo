package pl.pwr.Neuralingo.dto.docContent;

public record Line(String content, int pageNumber, BoundingBox boundingBox, float confidence) {}
