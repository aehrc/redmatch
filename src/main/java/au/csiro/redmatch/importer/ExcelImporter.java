package au.csiro.redmatch.importer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.model.Mapping;

/**
 * Imports mappings from Excel.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class ExcelImporter {

  private DecimalFormat decimalFormat = new DecimalFormat("#");

  private String getStringValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    String res = null;
    try {
      res = decimalFormat.format(cell.getNumericCellValue());
    } catch (IllegalStateException e) {
      try {
        res = cell.getStringCellValue();
      } catch (IllegalStateException e1) {
        throw new RedmatchException("Something went wrong getting the value of a cell.", e);
      }
    }
    return res;
  }

  /**
   * Returns the mappings defined in a spreadsheet.
   * 
   * @param wb The spreadsheat.
   * @return A map of mappings, indexed by mapping id.
   */
  public List<Mapping> importMappings(Workbook wb) {
    final List<Mapping> res = new ArrayList<>();
    Sheet sheet = wb.getSheetAt(0);
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);

      Cell idCell = row.getCell(0);
      Cell typeCell = row.getCell(1);
      Cell labelCell = row.getCell(2);
      Cell textCell = row.getCell(3);
      Cell systemCell = row.getCell(4);
      Cell codeCell = row.getCell(5);
      Cell displayCell = row.getCell(6);

      String id = idCell.getStringCellValue();
      String type = typeCell.getStringCellValue();
      String label = labelCell.getStringCellValue();
      String text = textCell.getStringCellValue();
      String system = systemCell.getStringCellValue();
      String code = getStringValue(codeCell);
      String display = getStringValue(displayCell);

      Mapping mapping = new Mapping();
      mapping.setRedcapFieldId(id);
      mapping.setRedcapLabel(label);
      mapping.setRedcapFieldType(type);
      mapping.setText(text);
      mapping.setTargetSystem(system);
      mapping.setTargetCode(code);
      mapping.setTargetDisplay(display);
      res.add(mapping);
    }
    return res;
  }

}
