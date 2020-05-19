/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.model.Mapping;

/**
 * Exports mappings to Excel.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class ExcelExporter {
  
  /**
   * Exports the mappings in a study in Excel format.
   * 
   * @param projectId The id of the Redmatch project. Used to name the tab.
   * @param mappings The mappings to export.
   * @param baos The output stream used to write the result.
   * 
   * @throws IOException If an IO error happens.
   */
  public void exportStudy(String projectId, List<Mapping> mappings, ByteArrayOutputStream baos)
      throws IOException {
    try (Workbook wb = new HSSFWorkbook()) {
      CellStyle unlockedCellStyle = wb.createCellStyle(); // Unlocked style to allow updates
      unlockedCellStyle.setLocked(false);
      unlockedCellStyle.setWrapText(true);

      CellStyle wrappedCellStyle = wb.createCellStyle(); // Wrapped style for label
      wrappedCellStyle.setWrapText(true);

      Font defaultFont = wb.createFont();
      defaultFont.setFontHeightInPoints((short) 10);
      defaultFont.setFontName("Arial");
      defaultFont.setColor(IndexedColors.BLACK.getIndex());
      defaultFont.setBold(false);
      defaultFont.setItalic(false);
      Font font = wb.createFont();
      font.setFontHeightInPoints((short) 10);
      font.setFontName("Arial");
      font.setColor(IndexedColors.BLACK.getIndex());
      font.setBold(true);
      font.setItalic(false);
      CellStyle boldStyle = wb.createCellStyle(); // Bold style for first row
      boldStyle.setFont(font);

      Sheet sheet = wb.createSheet(String.valueOf(projectId));
      sheet.createFreezePane(0, 1); // Freeze top row
      sheet.protectSheet("password"); // Protect to avoid unwanted changes

      Row row = sheet.createRow(0);
      Cell cell0 = row.createCell(0);
      cell0.setCellStyle(boldStyle);
      cell0.setCellValue("REDCap Field Id");
      Cell cell1 = row.createCell(1);
      cell1.setCellStyle(boldStyle);
      cell1.setCellValue("Type");
      Cell cell2 = row.createCell(2);
      cell2.setCellStyle(boldStyle);
      cell2.setCellValue("Label");
      Cell cell3 = row.createCell(3);
      cell3.setCellStyle(boldStyle);
      cell3.setCellValue("Text");
      Cell cell4 = row.createCell(4);
      cell4.setCellStyle(boldStyle);
      cell4.setCellValue("System");
      Cell cell5 = row.createCell(5);
      cell5.setCellStyle(boldStyle);
      cell5.setCellValue("Code");
      Cell cell6 = row.createCell(6);
      cell6.setCellStyle(boldStyle);
      cell6.setCellValue("Display");

      for (int i = 0; i < mappings.size(); i++) {
        row = sheet.createRow(i + 1);
        cell0 = row.createCell(0);
        
        Mapping mapping = mappings.get(i);
        cell0.setCellValue(mapping.getRedcapFieldId());
        cell1 = row.createCell(1);
        cell1.setCellValue(mapping.getRedcapFieldType());
        cell2 = row.createCell(2);
        cell2.setCellStyle(wrappedCellStyle);
        cell2.setCellValue(mapping.getRedcapLabel());
        cell3 = row.createCell(3);
        cell3.setCellStyle(wrappedCellStyle);
        cell3.setCellValue(mapping.getText());
        cell4 = row.createCell(4);
        cell4.setCellStyle(unlockedCellStyle);
        cell4.setCellValue(mapping.getTargetSystem());
        cell5 = row.createCell(5);
        cell5.setCellStyle(unlockedCellStyle);
        cell5.setCellValue(mapping.getTargetCode());
        cell6 = row.createCell(6);
        cell6.setCellStyle(unlockedCellStyle);
        cell6.setCellValue(mapping.getTargetDisplay());
      }

      sheet.autoSizeColumn(0);
      sheet.autoSizeColumn(1);
      sheet.setColumnWidth(2, 40 * 256);
      sheet.setColumnWidth(3, 40 * 256);
      sheet.setColumnWidth(4, 20 * 256);
      sheet.setColumnWidth(5, 20 * 256);
      sheet.setColumnWidth(6, 40 * 256);
      wb.write(baos);
    }
  }

}
