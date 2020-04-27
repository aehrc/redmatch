# Redmatch Reference

This document describes the Redmatch transformation rules grammar. A tutorial that shows how to run Redmatch and transform a REDCap form into FHIR resources is available [here](tutorial.md).

The grammar defines the structure of the transformation rules that are used by Redmatch to transform data in REDCap forms into FHIR resources. A Redmatch rules document is a collection of rules. Each rule has a condition and a body. If the condition is true, the body is executed. The tutorial makes use of a comprehensive set of rules and shows how different types of REDCap form elements can be translated to FHIR resources. The following sections show the different functions and operators that can be used in the rules.

## Rule

A rule is defined in the grammar as:

```
fcRule
    : repeatsClause? condition fcBody (ELSE fcBody)?
    ;
```

A rules document is a collection of rules. The order of the rules is not important.

## Condition

The Redmatch grammar defines a condition as:

```
condition
    : NOT condition
    | condition AND condition
    | condition OR condition
    | (TRUE | FALSE)
    | (NULL | NOTNULL) OPEN variableIdentifier CLOSE
    | VALUE OPEN variableIdentifier CLOSE(EQ | NEQ | LT | GT | LTE | GTE) (STRING | NUMBER)
    | OPEN condition CLOSE
    ;
```

### Literals

A condition can be defined simply by using a boolean literal. The following example shows how to run a rule every time:

```
TRUE { 
  ...
}
```

### Evaluating REDCap values

In most cases you will want to execute the body of a rule only if some condition is met. In the following example, the body of the rule is only executed if the value of the REDCap field `pat_dead` is equal to 1.

```
VALUE(pat_dead) = 1 {
  ...
}
```

Conditions can also be combined using the AND (&), OR (|) and NOT (^) operators.

### NULL and NOTNULL

Sometimes, the body of a rule should only be executed if a value has been entered in a REDCap field, regardless of the value. In this case the NOTNULL keyword can be used (NULL is just a shorthand for ^NOTNULL).

## Body

A body is defined in the grammar as:

```
fcBody
    : OPEN_CURLY (resource | fcRule)* CLOSE_CURLY
    ;
```

Most of the time a body will contain a resource element, which is used to create a FHIR resource and populate its attributes from values extracted from the REDCap forms, mappings defined by the user or literal values defined in the rules. However, a body can also contain a nested rule.

### Resource

A resource is defined in the grammar as:

```
resource
    : IDENTIFIER LT variableIdentifier GT THEN attribute EQ value (COMMA attribute EQ value)* END
    ;

attribute
    : IDENTIFIER (OPEN_SQ NUMBER CLOSE_SQ)? (DOT attribute)?
    ;

value
    : (TRUE | FALSE)
    | STRING
    | NUMBER
    | reference
    | CONCEPT_LITERAL OPEN_CODE CONCEPT_VALUE CLOSE_CODE
    | CODE_LITERAL OPEN_CODE CODE_VALUE CLOSE_CODE
    | (CONCEPT | CONCEPT_SELECTED | CODE_SELECTED | VALUE ) OPEN variableIdentifier CLOSE
    ;
    
reference
    : REF OPEN IDENTIFIER LT variableIdentifier GT CLOSE
    ;
```

A resource element defines the FHIR resource that gets created if the condition of the rule evaluates to `true` and assigns values to its attributes. The resource that gets created needs an identifier because it might be referred to by other rules. For example, the following snippet shows a Patient resource with an identifier of `p`.

```
Patient<p> -> ...
```

### Attribute

A resource element defines one or more attributes. Each attribute represents the path to an attribute in a FHIR resource. For example, the attribute `identifier.type.text` can be used to set the value of the `text` attribute in the codeable concept used to represent the type of an [identifier](https://www.hl7.org/fhir/datatypes.html#Identifier). The Redmatch compiler checks that these paths are valid and the application also creates any missing elements if the path refers to a nested attribute. When an attribute has multiplicity greater than one (i.e., the attribute is a list) the grammar supports referring to a specific index using square brackets. If a specific index is omitted, the application uses the first element in the list by default.

### Value

The values assigned to an attribute can be literals defined in the rules, references or values that come from REDCap. The Redmatch compiler checks that the values that are assigned are compatible with the attribute type in the FHIR resource.

#### Literals

Redmatch supports boolean, string and number literals. The following snippet shows how to assign a string literal value:

```
Patient<p> -> identifier.type.text = 'Medicare Number';
```

Also, two special kinds of literals are available: concept literals and code literals. The former can be used to populate `CodeableConcept` attributes and the latter to populate `code` attributes. The following snippet shows how to assign a codeable concept literal:

```
Patient<p> -> identifier.type = CONCEPT_LITERAL(http://hl7.org/fhir/v2/0203|MC);
```
#### References

FHIR supports attributes of type `reference` to point at other instances of FHIR resources. The Redmatch also allows assigning attributes of type `reference` to other FHIR resources that have been created with the rules. The following snippet shows how to assign a reference:

```
Condition<c> -> subject = REF(Patient<p>);
```

## Compatibility

The following table shows the Redmatch value expressions and the corresponding compatible REDCap fields and FHIR types in the Java reference implementation:

<table border="1">
   <tr>
    <td><b>Redmatch Expression</b></td>
    <td><b>Compatible REDCap fields</b></td>
    <td><b>Compatible FHIR attribute types</b></td>
  </tr>
  <tr>
    <td>TRUE, FALSE</td>
    <td>NA</td>
    <td>BooleanType</td>
  </tr>
  <tr>
    <td>CODE_LITERAL</td>
    <td>NA</td>
    <td>CodeType, Enumeration</td>
  </tr>
  <tr>
    <td>CONCEPT_LITERAL</td>
    <td>NA</td>
    <td>Coding, CodeableConcept</td>
  </tr>
  <tr>
    <td>NUMBER</td>
    <td>NA</td>
    <td>DecimalType</td>
  </tr>
  <tr>
    <td>NUMBER</td>
    <td>NA</td>
    <td>IntegerType</td>
  </tr>
  <tr>
    <td>REF</td>
    <td>NA</td>
    <td>Subclasses of DomainResource</td>
  </tr>
  <tr>
    <td>STRING</td>
    <td>NA</td>
    <td>StringType, MarkdownType, IdType, UriType, <br>
    OidType, UuidType, CanonicalType, UrlType</td>
  </tr>
  <tr>
    <td>CODE_SELECTED</td>
    <td>DROPDOWN, RADIO</td>
    <td>CodeType, Enumeration</td>
  </tr>
  <tr>
    <td>CONCEPT_SELECTED</td>
    <td>DROPDOWN, RADIO</td>
    <td>Coding, CodeableConcept</td>
  </tr>
  <tr>
    <td>CONCEPT</td>
    <td>TEXT*, YESNO, DROPDOWN, <br>
    RADIO, CHECKBOX, <br>
    CHECKBOX_OPTION, TRUEFALSE</td>
    <td>Coding, CodeableConcept</td>
  </tr>
  <tr>
    <td>VALUE</td>
    <td>TEXT, NOTES, CALC</td>
    <td>All FHIR primitive types. See <a 
    href="https://www.hl7.org/fhir/datatypes.html#primitive">here</a>.</td>
  </tr>
</table>

\* Only when the TEXT fields are configured to use validation based on FHIR ontologies. See [this REDCap plugin](https://github.com/aehrc/redcap_fhir_ontology_provider).
