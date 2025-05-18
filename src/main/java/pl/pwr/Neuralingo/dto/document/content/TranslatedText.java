package pl.pwr.Neuralingo.dto.document.content;

import java.util.List;

public class TranslatedText {
    public static class Paragraph {
        public int index;
        public String text;
        public boolean isTableRow = false;
        public List<String> texts; // przetłumaczone komórki tabeli (jeśli dotyczy)

        public Paragraph(int index, String text) {
            this.index = index;
            this.text = text;
            this.isTableRow = false;
        }

        public Paragraph(int index, List<String> texts) {
            this.index = index;
            this.texts = texts;
            this.text = String.join(" | ", texts);
            this.isTableRow = true;
        }
    }

    public List<Paragraph> paragraphs;

    public TranslatedText(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }
}
