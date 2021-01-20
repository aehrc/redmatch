/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;

/**
 * Describes what changed in REDCap.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class RedcapChanges {
  
  private boolean metadataChanged;
  
  private boolean dataChanged;
  
  public RedcapChanges() {
    
  }
  
  /**
   * Constructor.
   * 
   * @param metadataChanged True if metadata has changed.
   * @param dataChanged True if data has changed.
   */
  public RedcapChanges(boolean metadataChanged, boolean dataChanged) {
    super();
    this.metadataChanged = metadataChanged;
    this.dataChanged = dataChanged;
  }

  public boolean isMetadataChanged() {
    return metadataChanged;
  }

  public void setMetadataChanged(boolean metadataChanged) {
    this.metadataChanged = metadataChanged;
  }

  public boolean isDataChanged() {
    return dataChanged;
  }

  public void setDataChanged(boolean dataChanged) {
    this.dataChanged = dataChanged;
  }
  
  public boolean isChanged() {
    return dataChanged || metadataChanged;
  }
  
}
