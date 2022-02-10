/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.model.VersionedFhirPackage;
import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Miscellaneous utilities to deal with FHIR.
 *
 * @author Alejandro Metke Jimenez
 */
public class FhirUtils {

  private static final Log log = LogFactory.getLog(FhirUtils.class);

  /**
   * Returns the structure definitions in a FHIR package.
   *
   * @param ctx The FHIR context.
   * @param fhirPackage The FHIR package.
   * @return The list of structure definitions in the package.
   * @throws IOException If an I/O error occurs.
   */
  public static List<StructureDefinition> getStructureDefinitions(FhirContext ctx, VersionedFhirPackage fhirPackage)
    throws IOException {
    try (Stream<Path> paths = Files.walk(Paths.get(
      System.getProperty("user.home"),
      ".fhir",
      "packages",
      fhirPackage.toString(),
      "package"
    ))) {
      return paths
        .filter(Files::isRegularFile)
        .map(Path::toFile)
        .filter(f -> f.getName().endsWith(".json"))
        .filter(f -> f.getName().startsWith("StructureDefinition"))
        .filter(f -> {
          try (FileReader reader = new FileReader(f)) {
            StructureDefinition structureDefinition = (StructureDefinition) ctx.newJsonParser().parseResource(reader);
            return structureDefinition.hasSnapshot();
          } catch (Exception e) {
            log.warn("There was a problem with " + f.getName(), e);
            return false;
          }
        })
        .map(f -> {
          try (FileReader reader = new FileReader(f)) {
            return (StructureDefinition) ctx.newJsonParser().parseResource(reader);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .filter(s -> !s.getAbstract())
        .collect(Collectors.toList());
    }
  }

  /**
   * Returns all the resources referenced by a {@link Resource}. Assumes the {@link Reference}s have the actual target
   * resource set. If not, then it throws a {@link RuntimeException}.
   *
   * @param r The resource.
   * @return The referenced resources.
   */
  @SuppressWarnings("rawtypes")
  public static Collection<Target> getReferencedResources(Resource r) {
    try {
      Set<Target> res = new HashSet<>();
      Queue<Base> bases = new LinkedList<>();
      bases.add(r);

      while (!bases.isEmpty()) {
        Base base = bases.remove();
        for (Field field : base.getClass().getDeclaredFields()) {
          field.setAccessible(true);
          Object value = field.get(base);
          String attributeName = field.getName();
          if (value != null) {
            if (value instanceof Reference) {
              res.add(getReferenceTarget((Reference) value, attributeName));
            } else if (value instanceof BackboneElement) {
              bases.add((Base) value);
            } else if (value instanceof List) {
              for (Object o : (List) value) {
                if (o instanceof Reference) {
                  res.add(getReferenceTarget((Reference) o, attributeName));
                } else if (o instanceof Base) {
                  bases.add((Base) o);
                }
              }
            }
          }
        }
      }
      return res;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Target getReferenceTarget(Reference ref, String attributeName) {
    Resource tgt = (Resource) ref.getResource();
    if (tgt != null) {
      String resourceId = tgt.fhirType() + "<" + tgt.getId() + ">";
      return new Target(resourceId, attributeName);
    } else if(ref.getReference() != null) {
      // Assume we have a textual reference
      List<String> idParts = new ArrayList<>();
      String[] parts = ref.getReference().split("[/]");
      for (String part : parts) {
        if (!part.isEmpty()) {
          idParts.add(part);
        }
      }
      if (idParts.size() != 2) {
        throw new RuntimeException("Found malformed reference target: " + ref.getReference());
      }
      String resourceId = idParts.get(0) + "<" + idParts.get(1) + ">";
      return new Target(resourceId, attributeName);
    } else {
      throw new RuntimeException("Found reference with no actual target resource set");
    }
  }

  public static class Target {
    private final String resourceId;
    private final String attributeName;

    public Target(String resourceId, String attributeName) {
      this.resourceId = resourceId;
      this.attributeName = attributeName;
    }

    public String getResourceId() {
      return resourceId;
    }

    public String getAttributeName() {
      return attributeName;
    }
  }
}
