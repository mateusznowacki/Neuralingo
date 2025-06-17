package pl.pwr.Neuralingo.translation.file.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTextExtractorTest {

    @Test
    void extractText_extractsAllNonEmptyStringsFromCells() throws Exception {
        // Create a temp Excel file
        File tempFile = Files.createTempFile("test_excel", ".xlsx").toFile();
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet1 = workbook.createSheet("Sheet1");
            Row row0 = sheet1.createRow(0);
            row0.createCell(0).setCellValue("Hello");
            row0.createCell(1).setCellValue(" ");  // blank cell (should be ignored)
            row0.createCell(2).setCellValue("World");

            Row row1 = sheet1.createRow(1);
            row1.createCell(0).setCellValue(""); // empty string cell (ignored)
            row1.createCell(1).setCellValue("Test");

            // Add another sheet with data
            Sheet sheet2 = workbook.createSheet("SecondSheet");
            Row row0Sheet2 = sheet2.createRow(0);
            row0Sheet2.createCell(0).setCellValue("Another");
            row0Sheet2.createCell(1).setCellValue("Entry");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        ExcelTextExtractor extractor = new ExcelTextExtractor();
        ExtractedText extracted = extractor.extractText(tempFile);

        List<Paragraph> paragraphs = extracted.getParagraphs();

        // Should extract only non-empty trimmed strings
        assertEquals(5, paragraphs.size());

        // Check texts only (order and content)
        assertEquals("Hello", paragraphs.get(0).getText());
        assertEquals("World", paragraphs.get(1).getText());
        assertEquals("Test", paragraphs.get(2).getText());
        assertEquals("Another", paragraphs.get(3).getText());
        assertEquals("Entry", paragraphs.get(4).getText());
    }
}
