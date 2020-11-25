/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
