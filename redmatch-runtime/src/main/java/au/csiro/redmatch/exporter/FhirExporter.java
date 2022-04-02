/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.hl7.fhir.r4.model.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Transforms REDCap data into FHIR resources using a transformation rules document.
 * 
 * @author Alejandro Metke Jimenez
 */
public class FhirExporter {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporter.class);

  private final HapiReflectionHelper helper;

  private final TerminologyService terminologyService;

  private final VersionedFhirPackage defaultFhirPackage;

  /**
   * The transformation rules document.
   */
  private final Document doc;

  /**
   * The source data.
   */
  private final List<Row> rows;



  /**
   * The result of the transformation.
   */
  private final Map<String, DomainResource> fhirResourceMap = new HashMap<>();

  /**
   * Constructor.
   *
   * @param doc The transformation rules document.
   * @param rows The source data.
   * @param helper The HAPI transformation helper instance.
   */
  public FhirExporter(Document doc, List<Row> rows, HapiReflectionHelper helper,
                      TerminologyService terminologyService, VersionedFhirPackage defaultFhirPackage) {
    this.doc = doc;
    this.rows = rows;
    this.helper = helper;
    this.terminologyService = terminologyService;
    this.defaultFhirPackage = defaultFhirPackage;
  }

  /**
   * Creates FHIR resources based on data from the source. Returns a map, indexed by resource id.
   *
   * @param progressReporter Used to report progress.
   * @param cancelToken Used to check if the user has canceled the operation.
   * @return The map of created resources, indexed by resource id.
   */
  public Map<String, DomainResource> transform(ProgressReporter progressReporter, CancelChecker cancelToken)
    throws TransformationException {
    final String uniqueField = doc.getSchema().getUniqueFieldId();
    log.info("Transforming Redmatch project using unique field " + uniqueField);

    log.debug("Building graph to determine which resources are created by patient and which are not");
    GraphUtils.Results res = GraphUtils.buildGraph(doc);
    if (!res.getDiagnostics().isEmpty()) {
      boolean hasErrors = false;
      StringBuilder sb = new StringBuilder();
      for(Diagnostic d : res.getDiagnostics()) {
        if (d.getSeverity().equals(DiagnosticSeverity.Error)) {
          hasErrors = true;
          sb.append(d.getMessage());
          sb.append(System.lineSeparator());
        }
      }
      if (hasErrors) {
        throw new TransformationException(sb.toString());
      }
    }

    if (cancelToken != null && cancelToken.isCanceled()) {
      throw new TransformationException("Transformation canceled!");
    }

    try {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Transforming into FHIR"));
      }

      RedcapVisitor visitor = new RedcapVisitor(doc, res.getUniqueIds(), helper, terminologyService,
        defaultFhirPackage, rows, progressReporter, cancelToken);
      fhirResourceMap.putAll(visitor.getFhirResourceMap());

      // Prune resources to get rid of empty values in lists
      for (DomainResource c : fhirResourceMap.values()) {
        prune(c);
      }

      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportProgress(100));
      }

      return fhirResourceMap;
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }



  /**
   * Removes any empty attributes that might have been created because of the rules.
   * 
   * @param base The resource to prune.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void prune(Base base) {
    log.trace("Pruning " + base);
    // Find attributes where values are set
    final Set<String> setAttrs = new HashSet<>();
    Class<? extends Base> c = base.getClass();
    for (Method m : c.getMethods()) {
      try {
        String methodName = m.getName();
        if (!methodName.equals("hasPrimitiveValue")
          && methodName.startsWith("has")
          && Character.isUpperCase(methodName.charAt(3))
          && m.getParameterCount() == 0
          && ((Boolean) m.invoke(base, new Object[0]))) {
          setAttrs.add(m.getName().substring(3));
        }
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
      }
    }
    log.trace("The following attributes are set: " + setAttrs);

    // Get set attributes with multiplicity > 1
    final Set<String> multSetAttrs = new HashSet<>();
    for (Method m : c.getMethods()) {
      if (m.getName().startsWith("add") && setAttrs.contains(m.getName().substring(3))) {
        multSetAttrs.add(m.getName().substring(3));
      }
    }
    log.trace("The following attributes with multiplicity > 1 are set: " + multSetAttrs);

    // Prune those attributes
    log.trace("Pruning attributes with multiplicity > 1");
    for (String att : multSetAttrs) {
      try {
        List<Base> list = (List<Base>) c.getMethod("get" + att).invoke(base,
            new Object[0]);
        List<Base> toDelete = new ArrayList<>();
        for (Base b : list) {
          if (b.isEmpty()) {
            toDelete.add(b);
          }
        }
        list.removeAll(toDelete);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
      }
    }

    // Prune recursively - call the method for every attribute that is set and is not a primitive
    // type
    log.trace("Pruning recursively");
    for (String attr : setAttrs) {
      try {
        log.trace("Invoking method get" + attr + " on " + base);
        Object o = c.getMethod("get" + attr).invoke(base);
        log.trace("Got object of type " + o.getClass());
        if (o instanceof Base) {
          Base b = (Base) o;
          if (!helper.isPrimitive(b.getClass())) {
            prune((Base) o);
          }
        } else if (o instanceof List) {
          for (Object oo : (List) o) {
            if (oo instanceof Base) {
              prune((Base) oo);
            }
          }
        }
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
      }
    }
  }
}
