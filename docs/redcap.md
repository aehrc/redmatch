# Setting Up REDCap

This document will describe how to set up the REDCap project used in the tutorial. You need to have access to a REDCap instance and have the right permissions to create a project. Installation instructions for REDCap can be found [here](https://projectredcap.org/software/requirements/). If you use Docker there is also a GitHub project that shows how to spin up an instance of REDCap using `docker-compose`, available at [https://github.com/123andy/redcap-docker-compose](https://github.com/123andy/redcap-docker-compose). This tutorial was developed using REDCap version 10.0.15.

Also, the tutorial uses the [FHIR Ontology External Module](https://github.com/aehrc/redcap_fhir_ontology_provider). The external module is available in the official [REDCap Repository of External Modules](https://redcap.vanderbilt.edu/consortium/modules/) and can be installed directly from REDCap. Please refer to the REDCap documentation for details on the installation procedure. The module's [GitHub page](https://github.com/aehrc/redcap_fhir_ontology_provider) has documentation on configuration and usage.

Once you have access to a REDCap instance you need to create a new project. A sample project with data is available in the [RedmatchTutorial_2021-11-15_1444.REDCap.xml](./files/RedmatchTutorial_2021-11-15_1444.REDCap.xml) file. To create the project, click on the "New Project" button in the top menu and create a new REDCap project as shown here:

![Create new REDCap project](img/redcap_create_project.png?raw=true "Create new REDCap project")

Click on the "Choose File" button and select the RedmatchTutorial_2021-11-15_1444.REDCap.xml file.

If the import is successful you can now inspect the form that was created by navigating to the "Online Designer" page on your project. You should have a single form called Patient Information, as shown here:

![Patient information form](img/redcap_patient_information_form.png?raw=true "Patient information form")

If you click on the "Patient Information" link you will be able to explore the form definition, as shown here:

![Patient information form details](img/redcap_patient_information_form_detail.png?raw=true "Patient information form details")

Finally, Redmatch communicates with REDCap through its API and requires a token to authenticate. You need to request this token to your REDCap administrator. If you are using a local instance you can generate an API token by clicking on the "API" button on the left hand side menu and then on the "Create API token now" button. The token should look like this:

![REDCap API token](img/redcap_api_token.png?raw=true "REDCap API token")

[Home](./index.html)