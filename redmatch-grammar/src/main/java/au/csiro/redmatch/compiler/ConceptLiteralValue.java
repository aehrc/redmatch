/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A concept literal. This has the form system | code | display.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class ConceptLiteralValue extends Value {
  
  private String system;
  private String code;
  private String display;

  public ConceptLiteralValue(String system, String code, String display) {
    super();
    
    try {
      URI.create(system);
    } catch (IllegalArgumentException e) {
      throw new InvalidSyntaxException("System " + system + " is not a valid URI.");
    }

    Pattern codePattern = Pattern.compile("[^\\s]+(\\s[^\\s]+)*");
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
    return system + "|" + code + (display != null ? "|" + display : "");
  }
  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
