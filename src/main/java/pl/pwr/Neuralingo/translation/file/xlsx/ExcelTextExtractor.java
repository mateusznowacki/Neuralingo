package pl.pwr.Neuralingo.translation.file.xlsx;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class ExcelTextExtractor {

    public ExtractedText extractText(File file) throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();
        int index = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String text = cell.getStringCellValue().trim();
                            if (!text.isEmpty()) {
                                paragraphs.add(new Paragraph(index++, text, sheetIndex, row.getRowNum(), cell.getColumnIndex()));
                            }
                        }
                    }
                }
            }
        }

        return new ExtractedText(paragraphs);
    }
}