/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Miscellaneous utilities to deal with FHIR.
 *
 * @author Alejandro Metke Jimenez
 */
public class FhirUtils {

    /**
     * Returns all the resources referenced by a {@link Resource}. Assumes the {@link Reference}s have the actual target
     * resource set. If not, then it throws a {@link RuntimeException}.
     *
     * @param r The resource.
     * @return The referenced resources.
     * @throws IllegalAccessException
     */
    public static Collection<Resource> getReferencedResources(Resource r) {
        try {
            Set<Resource> res = new HashSet<>();
            Queue<Base> bases = new LinkedList<>();
            bases.add(r);

            while (!bases.isEmpty()) {
                Base base = bases.remove();
                for (Field field : base.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = field.get(base);
                    if (value != null) {
                        if (value instanceof Reference) {
                            res.add(getReferenceTarget((Reference) value));
                        } else if (value instanceof BackboneElement) {
                            System.out.println("in backbone");
                            bases.add((Base) value);
                        } else if (value instanceof List) {
                            for (Object o : (List) value) {
                                if (o instanceof Reference) {
                                    res.add(getReferenceTarget((Reference) o));
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

    private static Resource getReferenceTarget(Reference ref) {
        Resource tgt = (Resource) ref.getResource();
        if (tgt == null) {
            throw new RuntimeException ("Found reference with not actual target resource set: " + tgt.toString());
        }
        return tgt;
    }
}
