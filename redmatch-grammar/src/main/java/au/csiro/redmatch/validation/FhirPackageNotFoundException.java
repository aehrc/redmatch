/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import au.csiro.redmatch.model.VersionedFhirPackage;

/**
 * Thrown when a FHIR package cannot be found in the Firely registry.
 *
 * @author Alejandro Metke-Jimenez
 */
public class FhirPackageNotFoundException extends RuntimeException {

  private final VersionedFhirPackage fhirPackage;

  public FhirPackageNotFoundException(VersionedFhirPackage fhirPackage, Throwable cause) {
    super(cause);
    this.fhirPackage = fhirPackage;
  }

  public FhirPackageNotFoundException(VersionedFhirPackage fhirPackage) {
    this(fhirPackage, null);
  }

  public VersionedFhirPackage getFhirPackage() {
    return fhirPackage;
  }
}
