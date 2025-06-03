package pl.pwr.Neuralingo.dto.document.content;


public class Paragraph {

    private final int index;
    private final String text;
    private int sheetIndex;
    private int rowIndex;
    private int columnIndex;
    private Integer slideIndex;
    private int pageIndex;
    private float x;
    private float y;
    private float width;
    private float height;
    private String font;


    public Paragraph(String text, int sheetIndex, int rowIndex, int columnIndex, Integer slideIndex, int pageIndex, float x, float y, float width, float height, String font, int index) {
        this.text = text;
        this.sheetIndex = sheetIndex;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.slideIndex = slideIndex;
        this.pageIndex = pageIndex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.index = index;
    }

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


    public int getPageIndex() {
        return pageIndex;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getFont() {
        return font;
    }
}


