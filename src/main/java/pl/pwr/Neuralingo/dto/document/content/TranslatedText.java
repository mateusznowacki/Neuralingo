package pl.pwr.Neuralingo.dto.document.content;

import java.util.List;


public class TranslatedText {
    private final List<Paragraph> paragraphs;

    public TranslatedText(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }
}
