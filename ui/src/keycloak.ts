import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  url: 'https://auth.ontoserver.csiro.au/auth',
  realm: 'aehrc',
  clientId: 'redmatch',
});

export default keycloak;