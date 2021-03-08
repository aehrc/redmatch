# Redmatch Configuration

This document describes the Redmatch configuration options available for the backend and the frontend. A tutorial that shows how to run Redmatch and transform a REDCap form into FHIR resources is available [here](tutorial.md).

## Backend

The following properties can be set in the backend component: 

| Property                                              | Default Value                       | Description                                                                                                                                          |
| ----------------------------------------------------- | ----------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| redmatch.targetFolder                                 | ${user.home}/redmatch               | Folder where the FHIR resources are generated.                                                                                                       |
| ontoserver.url                                        | https://r4.ontoserver.csiro.au/fhir | The URL of a FHIR terminology server. Required for validation.                                                                                       |
| redmatch.security.enabled                             | false                               | Indicates if security in the Redmatch backend is turned on or off.                                                                                   |
| spring.security.oauth2.resourceserver.jwt.issuer-uri  | none                                | The URL of an OpenID Connect Provider's configuration endpoint or an authorisation server's metadata endpoint.                                        |
| spring.security.oauth2.resourceserver.jwt.jwk-set-uri | none                                | The URL of the authorisation server endpoint were the JSON Web Key (JWK) Set can be obtained to verify the JSON Web Signature (JWS) of the ID token. |

## Frontend

The following properties can be set in the fronend component: 

| Property                                              | Default Value                       | Description                                                             |
| ----------------------------------------------------- | ----------------------------------- | ----------------------------------------------------------------------- |
| REACT_APP_TERMINOLOGY_URL                             | https://r4.ontoserver.csiro.au/fhir | The URL of a FHIR terminology server. Required to find concepts to map. |
| REACT_APP_KEYCLOAK_URL                                | http://localhost:10001/auth         | The Keycloak authorisation endpoint.                                    |
| REACT_APP_KEYCLOAK_REALM                              | Aehrc                               | The Keycloak realm.                                                     |
| REACT_APP_KEYCLOAK_CLIENT_ID                          | redmatch                            | The Keycloak client id for Redmatch.                                    |
