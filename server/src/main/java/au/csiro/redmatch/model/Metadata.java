/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

/**
 * Represents the metadata of a REDCap project.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Entity
public class Metadata {

  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;



  public Metadata() {

  }



}
