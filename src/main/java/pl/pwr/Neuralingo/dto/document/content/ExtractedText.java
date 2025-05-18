package pl.pwr.Neuralingo.dto.document.content;

import java.util.List;

public class ExtractedText {
    public static class Paragraph {
        public int index;
        public String text;
        public boolean isTableRow = false;
        public List<String> tableCells;

        public Paragraph(int index, String text) {
            this.index = index;
            this.text = text;
        }

        public Paragraph(int index, List<String> tableCells) {
            this.index = index;
            this.tableCells = tableCells;
            this.text = String.join(" | ", tableCells); // opcjonalny podgląd
            this.isTableRow = true;
        }
    }

    public List<Paragraph> paragraphs;

    public ExtractedText(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }
}
