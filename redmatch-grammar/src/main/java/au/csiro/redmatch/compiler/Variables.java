/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains variables and their values.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class Variables {
  
  private final Map<String, Integer> variableValueMap = new HashMap<>();
  
  /**
   * Constructor.
   */
  public Variables() {
    
  }
  
  /**
   * Creates a new {@link Variables} object and initialises it with the values
   * from a parent {@link Variables} object.
   * 
   * @param parent The parent variables.
   */
  public Variables(Variables parent) {
    variableValueMap.putAll(parent.variableValueMap);
  }
  
  /**
   * Indicates if the variable is present.
   * 
   * @param variable The variable.
   * @return True if present, false otherwise.
   */
  public boolean hasVariable(String variable) {
    return variableValueMap.containsKey(variable);
  }
  
  /**
   * Adds a variable and a value. Returns true if the same variable already exists.
   * 
   * @param variable The variable name, e.g., 'x'.
   * @param value The value of the variable, e.g., 1.
   * @return True if the variable already exists or false otherwise.
   */
  public boolean addVariable(String variable, int value) {
    return variableValueMap.put(variable, value) != null;
  }
  
  /**
   * Returns the value of a variable.
   * 
   * @param variable The variable.
   * @return The value.
   * @throws UnknownVariableException If the requested variable is not available.
   */
  public int getValue(String variable) {
    if (!variableValueMap.containsKey(variable)) {
      throw new UnknownVariableException("The variable '" + variable + "' was not found. Available "
          + "variables are: " + variableValueMap.keySet() + ".");
    }
    
    return variableValueMap.get(variable);
  }
  
}
