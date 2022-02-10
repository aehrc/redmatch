/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import au.csiro.redmatch.model.VersionedFhirPackage;

/**
 * Thrown when there is a problem downloading a FHIR package.
 *
 * @author Alejandro Metke-Jimenez
 */
public class FhirPackageDownloadException extends RuntimeException {
  private final VersionedFhirPackage fhirPackage;

  public FhirPackageDownloadException(VersionedFhirPackage fhirPackage, Throwable cause) {
    super(cause);
    this.fhirPackage = fhirPackage;
  }

  public VersionedFhirPackage getFhirPackage() {
    return fhirPackage;
  }
}
