/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A code literal.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class CodeLiteralValue extends Value {

  private String code;

  public CodeLiteralValue(String code) {
    super();
    
    // Validate code based on FHIR spec
    Pattern codePattern = Pattern.compile("[^\\s]+(\\s[^\\s]+)*");
    final Matcher matcher = codePattern.matcher(code);
    if (!matcher.matches()) {
      throw new InvalidSyntaxException("Code " + code + " is invalid. Codes should be strings which "
          + "have at least one character and no leading or trailing whitespace, and where there is "
          + "no whitespace other than single spaces in the contents.");
    }
    
    this.code = code;
  }

  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return "CodeLiteralValue [code=" + code + "]";
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
