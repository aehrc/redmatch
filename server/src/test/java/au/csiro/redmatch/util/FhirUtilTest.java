/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link FhirUtils} class.
 *
 * @author Alejandro Metke Jimenez
 */
public class FhirUtilTest {

    @Test
    public void testGetReferencedResources() throws IllegalAccessException {
        Patient p = new Patient();
        p.setId("p-1");

        Observation obs = new Observation();
        obs.setId("obs-1");
        obs.setStatus(Observation.ObservationStatus.FINAL);
        obs.setCode(new CodeableConcept().setText("something"));
        obs.setSubject(new Reference(p));

        Condition cond = new Condition();
        cond.setId("cond-1");
        cond.setSubject(new Reference(p));
        cond.addStage().addAssessment(new Reference(obs));

        Collection<Resource> res = FhirUtils.getReferencedResources(cond);
        assertEquals(2, res.size());

        res = FhirUtils.getReferencedResources(obs);
        assertEquals(1, res.size());

        res = FhirUtils.getReferencedResources(p);
        assertEquals(0, res.size());
    }
}
