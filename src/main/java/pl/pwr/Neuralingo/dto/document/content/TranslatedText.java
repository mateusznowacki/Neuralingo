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


    public Paragraph getByIndex(int index) {
        return paragraphs.stream()
                .filter(p -> p.getIndex() == index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "TranslatedText{" +
                "paragraphs=" + paragraphs +
                '}';
    }
}
