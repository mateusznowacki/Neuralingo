package pl.pwr.Neuralingo.dto.document.content;


public class Paragraph {

    private final int index;
    private final String text;
    private int sheetIndex;
    private int rowIndex;
    private int columnIndex;
    private Integer slideIndex;

    public Paragraph(int index, String text) {
        this.index = index;
        this.text = text;
    }

    // === Konstruktor dla PowerPoint ===
    public Paragraph(int index, String text, int slideIndex) {
        this.index = index;
        this.text = text;
        this.slideIndex = slideIndex;
    }

    public Paragraph(int index, String text, int sheetIndex, int rowIndex, int columnIndex) {
        this.index = index;
        this.text = text;
        this.sheetIndex = sheetIndex;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public Integer getSlideIndex() {
        return slideIndex;
    }

}


