package pl.pwr.Neuralingo.dto.document.content;

import java.util.List;

public class ExtractedText {

    private final List<Paragraph> paragraphs;

    public ExtractedText(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    @Override
    public String toString() {
        return "ExtractedText{" +
                "paragraphs=" + paragraphs +
                '}';
    }
}

