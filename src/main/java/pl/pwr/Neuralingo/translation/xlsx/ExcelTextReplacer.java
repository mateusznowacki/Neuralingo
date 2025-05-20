package pl.pwr.Neuralingo.translation.xlsx;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class ExcelTextReplacer {

    public File replaceText(File originalFile, ExtractedText original, TranslatedText translated) throws IOException {
        Map<Integer, String> translationMap = translated.getParagraphs().stream()
                .collect(Collectors.toMap(Paragraph::getIndex, Paragraph::getText));

        String outputPath = originalFile.getAbsolutePath().replace(".xlsx", "") + "_translated.xlsx";
        File outputFile = new File(outputPath);

        try (FileInputStream fis = new FileInputStream(originalFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (Paragraph para : original.getParagraphs()) {
                if (translationMap.containsKey(para.getIndex())) {
                    Sheet sheet = workbook.getSheetAt(para.getSheetIndex());
                    Row row = sheet.getRow(para.getRowIndex());
                    if (row == null) continue;
                    Cell cell = row.getCell(para.getColumnIndex());
                    if (cell == null) continue;
                    cell.setCellValue(translationMap.get(para.getIndex()));
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }

        return outputFile;
    }
}
