/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a row of data.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Row {
  
  /**
   * The key - value pairs in the row.
   */
  private Map<String, String> data = new HashMap<>();

  /**
   * @return the data
   */
  public Map<String, String> getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Map<String, String> data) {
    this.data = data;
  }

}
