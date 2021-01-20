# Redmatch <img src="ui/src/components/redmatch_logo.png?raw=true">
> Redmatch is a tool designed to transform REDCap forms into FHIR resources.

## Table of contents
* [General info](#general-info)
* [Screenshots](#screenshots)
* [Technologies](#technologies)
* [Usage](#usage)
* [Features](#features)
* [Status](#status)
* [Credits](#credits)
* [Contact](#contact)

## General info
In recent years, clinical trials and studies have increasingly started using electronic systems to capture data required to conduct a range of analyses, such as the effectiveness of a new treatment or its economic value. However, even though these tools allow creating electronic forms easily, they are not designed to capture clinical data, impose few constraints on what should be captured and also have limited data sharing capabilities. One of the most popular tools currently used to capture research data is [REDCap](https://www.project-redcap.org/), a web application created at Vanderbilt University. Redmatch is a tool designed to define transformation rules between REDCap forms and FHIR resources. This allows exporting REDCap content in a standardised way.

This tool is targeted at domain experts that know about FHIR but don't necessarily have programming language skills. The rules engine implements a domain specific language that allows defining the transformation rules without the need to write any code.

## Screenshots
[![Redmatch Projects](docs/img/redmatch_projects_th.png?raw=true)](docs/img/redmatch_projects.png)
[![Redmatch rules](docs/img/redmatch_rules_th.png?raw=true)](docs/img/redmatch_rules.png)
[![Redmatch mappings](docs/img/redmatch_mappings_th.png?raw=true)](docs/img/redmatch_mappings.png)

## Technologies
* Spring Boot
* HAPI FHIR
* ANTLR
* MongoDB
* Apache POI
* React

## Usage

Please check the [tutorial](docs/tutorial.md).

**Redmatch is experimental software. Use it at your own risk!** Take a look at the
full description of the current set of [known issues](https://github.com/aehrc/redmatch/issues).

## Features

* Domain specific language to define transformations between REDCap and FHIR
* Integration with REDCap through its API
* Support to define mappings to standardised terminologies
* Export to FHIR JSON-LD format, compatible with [Pathling](https://github.com/aehrc/pathling)

## Status
The project is in progress. It is being developed as part of the [Australian Genomics Clinical Phenotype Capture project](https://www.australiangenomics.org.au/our-research/a-national-approach-to-data-federation-and-analysis/#1557446974559-f278d56d-7ef6).

## Credits
This work is partly funded by [Australian Genomics](https://www.australiangenomics.org.au/). Australian Genomics is supported by the National Health and Medical Research Council (GNT1113531).

## Contact
Created by [@ametke](https://github.com/ametke).
