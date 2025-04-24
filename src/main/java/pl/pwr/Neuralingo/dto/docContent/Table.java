package pl.pwr.Neuralingo.dto.docContent;

import java.util.List;

public record Table(int pageNumber, List<List<String>> cells, BoundingBox boundingBox) {}
