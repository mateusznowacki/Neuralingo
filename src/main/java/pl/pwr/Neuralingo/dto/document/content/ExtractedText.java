package pl.pwr.Neuralingo.dto.document.content;

import java.util.List;

public class ExtractedText {
    public static class Paragraph {
        public int index;
        public String text;

        public Paragraph(int index, String text) {
            this.index = index;
            this.text = text;
        }
    }

    public List<Paragraph> paragraphs;

    public ExtractedText(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }
}
