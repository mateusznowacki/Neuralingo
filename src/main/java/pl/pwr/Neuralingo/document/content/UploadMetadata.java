package pl.pwr.Neuralingo.document.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UploadMetadata(String title, String sourceLanguage) {}
