/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A concept literal. This has the form system | code | display.
 * 
 * @author Alejandro Metke
 *
 */
public class ConceptLiteralValue extends Value {
  
  private String system;
  private String code;
  private String display;
  
  private final Pattern codePattern = Pattern.compile("[^\\s]+(\\s[^\\s]+)*");
  
  public ConceptLiteralValue(String system, String code, String display) {
    super();
    
    try {
      URI.create(system);
    } catch (IllegalArgumentException e) {
      throw new InvalidSyntaxException("System " + system + " is not a valid URI.");
    }
    
    final Matcher matcher = codePattern.matcher(code);
    if (!matcher.matches()) {
      throw new InvalidSyntaxException("Code " + code + " is invalid. Codes should be strings which "
          + "have at least one character and no leading or trailing whitespace, and where there is "
          + "no whitespace other than single spaces in the contents.");
    }
    
    this.system = system;
    this.code = code;
    this.display = display;
  }
  public ConceptLiteralValue(String system, String code) {
    this(system, code, null);
  }
  public String getSystem() {
    return system;
  }
  public void setSystem(String system) {
    this.system = system;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public String getDisplay() {
    return display;
  }
  public void setDisplay(String display) {
    this.display = display;
  }
  @Override
  public String toString() {
    return system + "|" + code + display != null ? ("|" + display) : "";
  }
  @Override
  public boolean referencesData() {
    return false;
  }
  
}
