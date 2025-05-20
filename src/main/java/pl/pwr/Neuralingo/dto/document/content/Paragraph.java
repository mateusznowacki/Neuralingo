package pl.pwr.Neuralingo.dto.document.content;


public class Paragraph {

    private final int index;
    private final String text;

    public Paragraph(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

}


