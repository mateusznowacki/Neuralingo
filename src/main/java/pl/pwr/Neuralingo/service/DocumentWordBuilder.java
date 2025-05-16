
import org.apache.poi.xwpf.usermodel.*;
import pl.pwr.Neuralingo.dto.document.content.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class DocumentWordBuilder {

//    public void buildWord(ExtractedDocumentContentDto contentDto, String outputPath) throws IOException {
//
//        XWPFDocument document = new XWPFDocument();
//
//        // 1️⃣ PARAGRAFY
//        for (ParagraphDto paragraph : contentDto.paragraphs()) {
//            XWPFParagraph para = document.createParagraph();
//            XWPFRun run = para.createRun();
//            run.setText(paragraph.content());
//
//            // Styl paragrafu
//            StyleDto style = paragraph.style();
//            applyStyle(run, style);
//        }
//
//        // 2️⃣ TABELKI
//        for (TableDto table : contentDto.tables()) {
//            XWPFTable xwpfTable = document.createTable(table.rowCount(), table.columnCount());
//
//            for (TableCellDto cell : table.cells()) {
//                XWPFTableCell xwpfCell = xwpfTable.getRow(cell.rowIndex()).getCell(cell.columnIndex());
//
//                // Usuwamy domyślny paragraf z komórki (Word tak tworzy)
//                xwpfCell.removeParagraph(0);
//                XWPFParagraph cellPara = xwpfCell.addParagraph();
//                XWPFRun run = cellPara.createRun();
//                run.setText(cell.content());
//
//                // Styl (jeśli kiedyś dodasz styl do TableCellDto)
//                if (cell.style() != null) {
//                    applyStyle(run, cell.style());
//                }
//            }
//        }
//
//        // 3️⃣ LISTY
//        for (ListItemDto item : contentDto.listItems()) {
//            XWPFParagraph para = document.createParagraph();
//            para.setStyle("ListBullet");
//            XWPFRun run = para.createRun();
//            run.setText(item.content());
//
//            // Styl listy (jeśli dodasz styl do ListItemDto)
//            if (item.style() != null) {
//                applyStyle(run, item.style());
//            }
//        }
//
//        // 4️⃣ KEY-VALUE PAIRS
//        for (KeyValuePairDto pair : contentDto.keyValuePairs()) {
//            XWPFParagraph para = document.createParagraph();
//            XWPFRun runKey = para.createRun();
//            runKey.setBold(true);
//            runKey.setText(pair.key().content() + ": ");
//
//            XWPFRun runValue = para.createRun();
//            runValue.setText(pair.value() != null ? pair.value().content() : "");
//        }
//
//        // 5️⃣ FIGURY
//        for (FigureDto figure : contentDto.figures()) {
//            XWPFParagraph para = document.createParagraph();
//            XWPFRun run = para.createRun();
//            run.setItalic(true);
//            run.setText("Figure: " + figure.description());
//        }
//
//        // 6️⃣ Zapis do pliku
//        try (FileOutputStream out = new FileOutputStream(outputPath)) {
//            document.write(out);
//        }
//    }
//
//    /**
//     * Metoda pomocnicza do stosowania stylu (bold, italic, font family)
//     */
//    private void applyStyle(XWPFRun run, StyleDto style) {
//        if (style == null) return;
//
//        if ("bold".equalsIgnoreCase(style.fontWeight())) {
//            run.setBold(true);
//        }
//        if ("italic".equalsIgnoreCase(style.fontStyle())) {
//            run.setItalic(true);
//        }
//        if (style.similarFontFamily() != null) {
//            run.setFontFamily(style.similarFontFamily());
//        }
//    }
}
