/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

/**
 * @author Alejandro Metke
 *
 */
public class RedmatchGrammarValidatorTest {
  
  @Test
  public void testGetFirstExtension() {
    RedmatchGrammarValidator val = new RedmatchGrammarValidator(null);
    assertEquals("Encounter.extension", val.getFirstExtension("Encounter.extension"));
    assertEquals("Encounter.extension", 
        val.getFirstExtension("Encounter.extension.valueQuantity.extension.url"));
    assertEquals("Encounter.extension", 
        val.getFirstExtension("Encounter.extension.valueQuantity.system"));
    assertEquals("Encounter.extension.url", val.getFirstExtension("Encounter.extension.url"));
  }
  
  @Test
  public void testGetOtherExtensions() {
    RedmatchGrammarValidator val = new RedmatchGrammarValidator(null);
    List<String> res = new ArrayList<>();
    val.getOtherExtensions("Encounter.extension", res);
    assertThat(res, is(Collections.emptyList()));
    res.clear();
    
    val.getOtherExtensions("Encounter.extension.valueQuantity.extension.url", res);
    assertThat(res, is(Arrays.asList("Quantity.extension.url")));
    res.clear();
    
    val.getOtherExtensions("Encounter.extension.valueQuantity.system", res);
    assertThat(res, is(Arrays.asList("Quantity.system")));
    res.clear();
    
    val.getOtherExtensions("Encounter.extension.url", res);
    assertThat(res, is(Collections.emptyList()));
    res.clear();
    
    val.getOtherExtensions("Observation.valueRatio.extension.valueRatio.extension.url", res);
    assertThat(res, is(Arrays.asList("Ratio.extension.url")));
    res.clear();
    
    val.getOtherExtensions("Encounter.extension.valueQuantity.extension.valueQuantity.extension."
        + "valueQuantity.system", res);
    assertThat(res, is(Arrays.asList("Quantity.extension", "Quantity.extension", 
        "Quantity.system")));
    res.clear();
    
  }

}
