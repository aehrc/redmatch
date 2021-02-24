/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import au.csiro.redmatch.model.RedmatchProject;

/**
 * Data access object for Redmatch projects.
 *
 * @author Alejandro Metke
 *
 */
public interface RedmatchDao extends CrudRepository<RedmatchProject, String> {

  public Optional<RedmatchProject> findById(String s);

  public Iterable<RedmatchProject> findAll();

  public boolean existsById(String s);

}
