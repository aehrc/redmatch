/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import env from '@beam-australia/react-env';
import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  url: env('KEYCLOAK_URL'),
  realm: env('KEYCLOAK_REALM'),
  clientId: env('KEYCLOAK_CLIENT_ID'),
});

export default keycloak;