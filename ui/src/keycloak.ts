/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import Keycloak from 'keycloak-js';

const dev = process.env.NODE_ENV === 'development' ? true : false;
const url = dev ? process.env.REACT_APP_KEYCLOAK_URL : (window as any)._env.REACT_APP_KEYCLOAK_URL;
const realm = dev ? process.env.REACT_APP_KEYCLOAK_REALM : (window as any)._env.REACT_APP_KEYCLOAK_REALM;
const clientId = dev ? process.env.REACT_APP_KEYCLOAK_CLIENT_ID : (window as any)._env.REACT_APP_KEYCLOAK_CLIENT_ID;

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  url: url,
  realm: realm,
  clientId: clientId,
});

export default keycloak;