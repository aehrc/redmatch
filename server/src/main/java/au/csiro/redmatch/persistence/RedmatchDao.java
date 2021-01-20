/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.persistence;

import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.DeleteResult;

import au.csiro.redmatch.api.ProjectNotFoundException;
import au.csiro.redmatch.model.RedmatchProject;

/**
 * @author Alejandro Metke
 *
 */
@Component
public class RedmatchDao {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchDao.class);
  
  @Autowired
  private MongoOperations ops;
  
  public Optional<RedmatchProject> getRedmatchProject(String id) {
    if (id == null) {
      return Optional.empty();
    }
    RedmatchProject res = ops.findById(id, RedmatchProject.class);
    return Optional.ofNullable(res);
  }
  
  public List<RedmatchProject> findRedmatchProjects() {
    return ops.findAll(RedmatchProject.class);
  }
  
  public void saveRedmatchProject(RedmatchProject project) {
    ops.save(project);
  }
  
  public void deleteRedmatchProject(String id) {
    final RedmatchProject fp = ops.findById(id, RedmatchProject.class);
    if (fp == null) {
      throw new ProjectNotFoundException("The Redmatch project with id " + id + " was not found.");
    }
    final DeleteResult res = ops.remove(fp);
    if(!res.wasAcknowledged()) {
      log.warn("Delete of Redmatch project with " + id + " was not acknowledged.");
    }
  }
  
}
