/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a row of data. The fields will be defined in a REDCap report and the
 * names should be in the corresponding {@link Metadata}.
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
