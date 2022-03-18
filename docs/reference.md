# Redmatch Transformation Rules Reference

This document describes the Redmatch transformation rules grammar. A tutorial that shows how to run Redmatch and transform a REDCap form into FHIR resources is available [here](./tutorial.md).

The grammar defines the structure of the rules that are used by Redmatch to transform data in REDCap forms into FHIR resources. The following sections describe the different sections in a rules document.

## Schema

This section declares where the Redmatch compiler can access the REDCap schema used for this rules document. This section is required because otherwise the compiler is unable to check if the rules are valid or not. The following example shows how to declare that the file `schema.json` in the current folder should be used as the schema:

```
SCHEMA: 'schema.json' (REDCAP)
```

The text in parentheses indicates that this is a REDCap schema. At the moment Redmatch only supports REDCap as a source of data but support for other sources is planned for future releases.

## Server

This section declares which REDCap server to use to fetch and transform data. It is a logical name that should correspond to a server definition in the `redmatch-config.yaml` configuration file. This avoids the need to include sensitive information in the rules document, such as the API token for REDCap. This section is optional while authoring the rules but required to be able to execute the transformation. The following is an example that declares that the `local` server should be used:

```
SERVER: 'local'
```

## Target

This section declares what is the target FHIR package that the rules will target. This is optional and when it is not present a default FHIR package will be used. This is currently set to FHIR R4. Note that if a FHIR package is not published in the Simplifier registry then it should be available locally in the `.fhir` folder (for example, if developing an IG locally).

```
TARGET: 'agha.fhir.genclipr#dev'
```

## Aliases

Aliases allow making the rules more readable, especially when long urls need to be reference frequently. The following example shows how to define aliases for SNOMED CT and the Human Phenotype Ontology. 

```
ALIASES: {
  $SCT = 'http://snomed.info/sct'
  $HPO = 'http://purl.obolibrary.org/obo/hp.fhir'
}
```

## Rules

Rules are used to declare how the fields in a REDCap form map to FHIR resources. The basic structure of a rule is the following:

```
REPEATS CONDITION BODY ELSE
```

The following sections describe each of these parts in detail.

### Repeats

This clause allows defining a variable so the same rule can be executed multiple times. This is useful when repeating fields are defined in REDCap and the same rule should be run for each field. The following is an example:

```
REPEAT(1..2: x)
    VALUE(dx_${x}) = 'NOT_FOUND' {
      ...
    }
```

In this case, the REDCap form has two fields named `dx_1` and `dx_2`. The clause defines the variable `x` and states that it takes on the values `1` and `2`. The variable can be referenced in the rules using the expression `${x}`.

### Condition

A condition defines when a rule should be run. Therefore, a condition expression always evaluates to a boolean value, i.e., `true` or `false`. There are three types of conditions:

- A boolean literal: `TRUE` or `FALSE`.
- A check for the presence or absence of the value of a REDCap field, regardless of what the value is: `NULL(field_id)` or `NOTNULL(field_id)`.
- The result of an operation with a value of a REDCap field: `VALUE(field_id) operator value`, e.g., `VALUE(dx_num) > 0`. The following operators are available: `=`, `!=`, `<`, `>`, `<=`, and `>=`.

Conditions can also be combined using the following logical operators: 
 - AND: `&`
 - OR: `|`

Finally, a condition can be negated by using the negation operator `^`.

### Body

The body of a rule runs when a condition evaluates to `true` and indicates which FHIR resource to populate and how. This is done using resource elements. A body can also contained nested rules.

#### Resources
A resource element defines the FHIR resource that gets created and assigns values to its attributes. The resource needs an identifier because it might be referenced by other rules. For example, the following snippet shows a Patient resource with an identifier of `p`. Note that this is an internal identifier used by Redmatch and it's different from an identifier of a FHIR resource.

```
Patient<p> : ...
```
### Attributes

A resource element defines one or more attributes. Each attribute represents the path to an attribute in a FHIR resource. For example, the attribute `identifier.type.text` can be used to set the value of the `text` attribute in the codeable concept used to represent the type of an [identifier](https://www.hl7.org/fhir/datatypes.html#Identifier). The Redmatch compiler checks that these paths are valid and also creates any missing elements if the path refers to a nested attribute. When an attribute has multiplicity greater than one (i.e., the attribute is a list) the grammar supports referring to a specific index using square brackets. If a specific index is omitted, the application uses the first element in the list by default.

### Values

The values assigned to an attribute can be literals, references or values that come from REDCap. The Redmatch compiler checks that the values that are assigned are compatible with the attribute type in the FHIR resource.

#### Literals

Redmatch supports boolean, string and number literals. The following snippet shows how to assign a string literal value:

```
Patient<p> : identifier.type.text = 'Medicare Number';
```

Also, two special kinds of literals are available: concept literals and codes. The former can be used to populate `CodeableConcept` attributes and the latter to populate `code` attributes. The following snippet shows how to assign a codeable concept literal:

```
Patient<p> : identifier.type = CONCEPT_LITERAL(http://hl7.org/fhir/v2/0203|MC);
```
#### References

FHIR supports attributes of type `reference` to point at other instances of FHIR resources. Redmatch also allows assigning attributes of type `reference` to other FHIR resources that have been created with the rules. The following snippet shows how to assign a reference:

```
Condition<c> : subject = REF(Patient<p>);
```

### Compatibility

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
    <td>CODE</td>
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
    <td>All <a 
    href="https://www.hl7.org/fhir/datatypes.html#primitive">FHIR primitive types</a>.</td>
  </tr>
</table>

\* Only when the TEXT fields are configured to use validation based on FHIR ontologies. See [this REDCap plugin](https://github.com/aehrc/redcap_fhir_ontology_provider).

### Else

The body of a rule executes if the condition evaluates to `true`. It is also possible to add another set of resources to create (or rules to evaluate) in an `else` block, and these will be executed when the condition evaluates to `false`. The following is an example that shows how to define an `else` block:

```
VALUE(dx_${x}) = 'NOT_FOUND' {
   ...
} ELSE {
  // This will run if the condition evaluates to 'false'
  ...
}
```

## Mappings

There are different ways of capturing coded data in a REDCap form. The preferred way is using the [FHIR Ontology External Module](https://github.com/aehrc/redcap_fhir_ontology_provider). Redmatch understands the values captured using the plugin and there is no need to define any mappings.

Coded data can also be captured using values defined in REDCap, using dropdown boxes, radio buttons or checkboxes. In all of these cases, the codes are not standardised and therefore Redmatch allows mapping these local codes to standardised terminologies.

The following is an example of mappings to SNOMED CT and HPO. Note that aliases can be used in the mapping definitions.

```
MAPPINGS: {
    pat_sex___1   -> $SCT|248153007|'Male';
    pat_sex___2   -> $SCT|248152002|'Female';
    phenotype___1 -> $HPO|HP:0000602|'Ophthalmoplegia';
}
```

Redmatch does not check if the mapping targets are valid codes. However, the resulting FHIR resources can be validated using a [FHIR instance validator](https://hapifhir.io/hapi-fhir/docs/validation/instance_validator.html).

[Home](./index.html)
