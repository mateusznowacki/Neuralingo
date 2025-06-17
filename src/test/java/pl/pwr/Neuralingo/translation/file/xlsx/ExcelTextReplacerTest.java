package pl.pwr.Neuralingo.translation.file.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTextReplacerTest {

    @Test
    void replaceText_replacesCellsWithTranslatedText() throws Exception {
        // Create temp Excel file
        File tempFile = Files.createTempFile("test_excel_replace", ".xlsx").toFile();
        tempFile.deleteOnExit();

        // Write initial data
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Hello");
            row0.createCell(1).setCellValue("World");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Test");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        // Original paragraphs with location info
        List<Paragraph> originalParagraphs = List.of(
                new Paragraph(0, "Hello", 0, 0, 0),  // sheet 0, row 0, col 0
                new Paragraph(1, "World", 0, 0, 1),  // sheet 0, row 0, col 1
                new Paragraph(2, "Test", 0, 1, 0)    // sheet 0, row 1, col 0
        );

        ExtractedText original = new ExtractedText(originalParagraphs);

        // Translated paragraphs (just index + text)
        List<Paragraph> translatedParagraphs = List.of(
                new Paragraph(0, "Cześć"),
                new Paragraph(1, "Świat"),
                new Paragraph(2, "Testowany")
        );
        TranslatedText translated = new TranslatedText(translatedParagraphs);

        ExcelTextReplacer replacer = new ExcelTextReplacer();
        File translatedFile = replacer.replaceText(tempFile, original, translated);

        assertTrue(translatedFile.exists());
        assertTrue(translatedFile.getName().endsWith("_translated.xlsx"));

        // Read back translated file and verify cell values
        try (FileInputStream fis = new FileInputStream(translatedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            Cell cell00 = sheet.getRow(0).getCell(0);
            Cell cell01 = sheet.getRow(0).getCell(1);
            Cell cell10 = sheet.getRow(1).getCell(0);

            assertEquals("Cześć", cell00.getStringCellValue());
            assertEquals("Świat", cell01.getStringCellValue());
            assertEquals("Testowany", cell10.getStringCellValue());
        }
    }

    @Test
    void replaceText_ignoresMissingRowsOrCells() throws Exception {
        File tempFile = Files.createTempFile("test_excel_replace_missing", ".xlsx").toFile();
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            // Only one row & cell
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Hello");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        // original with some missing rows/cells
        List<Paragraph> originalParagraphs = List.of(
                new Paragraph(0, "Hello", 0, 0, 0),       // exists
                new Paragraph(1, "MissingRow", 0, 1, 0),  // missing row 1
                new Paragraph(2, "MissingCell", 0, 0, 1)  // missing cell 1 in row 0
        );
        ExtractedText original = new ExtractedText(originalParagraphs);

        List<Paragraph> translatedParagraphs = List.of(
                new Paragraph(0, "Cześć"),
                new Paragraph(1, "Brak wiersza"),
                new Paragraph(2, "Brak komórki")
        );
        TranslatedText translated = new TranslatedText(translatedParagraphs);

        ExcelTextReplacer replacer = new ExcelTextReplacer();
        File translatedFile = replacer.replaceText(tempFile, original, translated);

        try (FileInputStream fis = new FileInputStream(translatedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row0 = sheet.getRow(0);

            assertEquals("Cześć", row0.getCell(0).getStringCellValue());
            assertNull(sheet.getRow(1));      // row 1 still missing
            assertNull(row0.getCell(1));      // cell 1 missing
        }
    }
}
