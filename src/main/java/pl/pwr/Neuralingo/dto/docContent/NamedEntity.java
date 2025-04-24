package pl.pwr.Neuralingo.dto.docContent;

public record NamedEntity(String category, String subCategory, String content, int pageNumber, BoundingBox boundingBox, float confidence) {}
