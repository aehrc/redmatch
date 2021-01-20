/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * An operation outcome with an additional status.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class OutcomeWithStatus {

  private final OperationOutcome outcome;

  private final String status;
  
  /**
   * Creates a new outcome with status.
   * 
   * @param outcome The outcome.
   * @param status The status.
   */
  public OutcomeWithStatus(OperationOutcome outcome, String status) {
    super();
    this.outcome = outcome;
    this.status = status;
  }

  public OperationOutcome getOutcome() {
    return outcome;
  }

  public String getStatus() {
    return status;
  }

}
