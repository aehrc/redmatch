# Tutorial

Redmatch is a tool designed to transform REDCap forms into Fast Healthcare Interoperability Resources (FHIR) format. REDCap is an Electronic Data Capture tool that allows easily creating forms to capture patient data. However, this flexibility means that data is usually captured in different ways across projects. Transforming REDCap data into FHIR enables analyses across multiple projects. To get started using Redmatch, you need to install and run it using the steps below.

## Step 1: Review Introduction

If you haven't read the [introduction page](./introduction.html), now is a good time to check it out.

## Step 2: Install Redmatch

If you haven't installed Redmatch have a look at the [installation instructions](./installation.html).

## Step 3: Set Up a REDCap Server and Sample Project

A REDCap server is required to run the tutorial. Instructions to install REDCap and create the sample project are available [here](./redcap.html).

## Step 4: Download the Tutorial Files

To help you get started please download the [tutorial starter](./files/tutorial_starter.zip) and unzip it in a folder of your choice.

## Step 5: Run the Redmatch Visual Studio Code Plugin

Open the tutorial folder in Visual Studio Code. Then, open the `tutorial.rdm` file. The plugin runs the validation in the background and displays the results as annotations in the editor.

The compiler checks that the rules are syntactically correct and also that the REDCap fields that are referenced in the rules exist. In this case, the field _patient\_dead_ does not exist in the REDCap form and therefore the compiler generates an error. Change the field name to _pat\_dead_ and now the rules should compile with no errors.

Let's go through the rules in more detail, using the second rule as an example:

```
VALUE(pat_dead) = 1 {
  NOTNULL(pat_dead_date) {
    Patient<p>: * deceasedDateTime = VALUE(pat_dead_date)
  } ELSE {
    Patient<p>: * deceasedBoolean = TRUE
  }
}
```

The first part of a rule is a condition that determines if the rule will run or not. In this case the condition is `VALUE(pat_dead) = 1`. This means that rule runs if the value of the REDCap field _pat\_dead_ is equal to _1_, which means that the _True_ radio button is selected.

The body of a rule is contained inside the curly brackets and indicates which FHIR resource should be created if the condition is true. Rules can also be nested, so the body of a rule can be another rule. In this case the sub-rule is evaluated if the REDCap field _pat_dead_date_ is non-empty, i.e., if the user has entered a date.

The sub-rule has both a body and an _ELSE_ clause. Similar to a programming language, if the condition is _true_ the main body is executed and otherwise the body in the _ELSE_ clause is. In both cases a _Patient_ resource, identified with the variable _p_, is created. The variable can be used to reference the same resource from different rules.

The lines after the colon are attributes of the resource. In this case the _deceased_ attribute is populated. Notice that because this is a value\[x\] type, it can be set to different types. The sub-rule states that if the main body is executed, the deceased attribute will be populated with the date the patient died. Otherwise, a boolean value will be used to indicate that the patient is deceased. Each line starts with an asterisk followed by the full path of the attribute to set, an equals sign and an expression. The compiler uses the FHIR meta-model to ensure these paths are valid.

There are several expressions that can be used to assign values to the attributes. In this example, two different expressions are used: a boolean literal and values form the form. A boolean literal is just a static boolean value (TRUE or FALSE). If the date when the patient died is set in the REDCap form then the expression `VALUE(pat_dead_date)` will extract the date from the REDCap field. Redmatch will try to transform the values in the forms to the data type of the target FHIR attribute, unless the types are not compatible, in which case a compiler error will be generated.

## Step 6: Add Rules To Transform Patient Diagnoses

We will now write additional rules to transform patient diagnoses. An example of the REDCap form is shown here:

![REDCap tutorial diagnoses](img/redmatch_tutorial_rules_dx.png?raw=true "REDCap tutorial diagnoses")

This section of the form uses REDCap's [FHIR Ontology External Module](https://github.com/aehrc/redcap_fhir_ontology_provider) to capture coded diagnoses. The plugin allows creating auto-complete style fields that use a FHIR terminology server to search for codes. The pattern that is implemented in this example also allows adding free text if a code is not found. The free text fields are hidden initially and are only displayed if the user explicitly indicates that a concept was not found. Also, because we want to capture one or more diagnoses, a dropdown and branching logic are used to display the number of diagnoses that user wants to enter.

Add the following to the end the rules section:

```
VALUE(dx_num) > 0 {
  REPEAT(1..2: x)
  NOTNULL(dx_${x}) {
    VALUE(dx_${x}) = '_NRF_' {
      Condition<c${x}>: 
        * code.text = VALUE(dx_text_${x})
        * subject = REF(Patient<p>)
    } ELSE {
      Condition<c${x}>: 
        * code = CONCEPT(dx_${x})
        * subject = REF(Patient<p>)
    }
  }
}
```

Let's look at these rules in more detail. The outermost condition, `VALUE(dx_num) > 0`, indicates that the rule should only run if one or more diagnoses have been entered. Inside this rule we find a sub-rule that gets repeated. The `REPEAT(1..2: x)` clause runs the rule twice and assigns `x` the value of `1` the first time it's run and `2` the second time. This variable is used to refer to REDCap fields that are repeated, such as `dx_1` and `dx_2`, without having to write separate rules for each repetition.

The condition `VALUE(dx_${x}) = '_NRF_'` looks for a predefined value used by the plugin that indicates that the user attempted to search for a code but couldn't find one. In this case, the `Condition.code` attribute in the target resource is populated with the free text that was entered. Otherwise, the `CONCEPT(dx_${x})` expression extracts the concept selected with the plugin.

The final thing worth pointing out is the use of references. The diagnoses that are generated should be linked back to the patient. This is achieved using the `REF(Patient<p>)` expression.


## Step 7: Add Rules to Transform Patient Phenotype

The final set of rules in this tutorial will transform a section of the form that uses checkboxes to capture a patient's phenotype into FHIR observations. An example of the REDCap form is shown here:

![REDCap tutorial phenotype](img/redmatch_tutorial_rules_phenotype.png?raw=true "REDCap tutorial phenotype")

Add the following to the end of your rules:

```
REPEAT(1..4: x)
NOTNULL(phenotype___${x}) {
  Observation<obs${x}>:
    * status = CODE(final)
    * code = CONCEPT(phenotype___${x})
    * interpretation = CONCEPT_LITERAL(http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation|POS)
    * subject = REF(Patient<p>)
}
```

In this case a repeat expression is used to test if each checkbox is checked or not, using the `NOTNULL(phenotype___${x})` expression. Each individual checkbox has an identifier that is made up of the main field id, in this case `phenotype`, and the value of each choice.

The `code` attribute is populated using the `CONCEPT` expression which tells Redmatch that this value should be coded. Because the checkboxes are just Redcap local codes, the platform creates a mapping that the user will need to complete before being able to export the data into FHIR. The `status` attribute is always the same regardless of the values in the REDCap form and there it is populated using the `CODE(final)` expression. This is also the case with the `interpretation` attribute, except that the data type in this case is `CodeableConcept` and therefore a `CONCEPT_LITERAL` expression is used.

## Step 8: Add the Missing Mappings

The phenotype is captured using checkboxes and therefore these codes are not standardised and need mapping. The Redmatch plugin will show which mappings are missing. Add the following mappings to the mappings section:

```
  phenotype___1 -> http://purl.obolibrary.org/obo/hp.owl|HP:0001558;
  phenotype___2 -> http://purl.obolibrary.org/obo/hp.owl|HP:0001270;
  phenotype___3 -> http://purl.obolibrary.org/obo/hp.owl|HP:0031910;
  phenotype___4 -> http://purl.obolibrary.org/obo/hp.owl|HP:0012587;
```

The rules should compile with no issues.

For more details and a list of all the expressions available in the language please check the [reference documentation](./reference.html).

## Step 9: Export to FHIR

If you are happy with the transformation rules you can run the transformation to produce FHIR resources. This can be done by opening the `tutorial.rdm` file, right-clicking on the content of the file and selecting the `Transform this file` command. 

This will generate a collection of files in [NDJSON](http://ndjson.org/) format in the `output` folder. Each resource type is saved in a separate file. In this case, three files are generated: `Condition.ndjson`, `Observation.ndjson` and `Patient.ndjson`. This formats allows directly importing this data into the [Pathling FHIR Analytics Platform](https://pathling.csiro.au/docs/import.html).

That's it! You have successfully transformed a REDCap project into standardised FHIR resources by defining transformation rules. To learn more about the Redmatch grammar checkout the [Redmatch reference documentation](reference.md).

[Home](./index.html)
