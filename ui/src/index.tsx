import React from 'react';
import ReactDOM from 'react-dom';

import { ReactKeycloakProvider } from '@react-keycloak/web'

import './index.css';
import keycloak from './keycloak'
import App from './components/App';
import * as serviceWorker from './serviceWorker';

const eventLogger = (event: unknown, error: unknown) => {
  console.log('onKeycloakEvent', event, error)
}

const tokenLogger = (tokens: unknown) => {
  console.log('onKeycloakTokens', tokens)
}

const init = {
  checkLoginIframe: false,
  enableLogging: true
}

ReactDOM.render(
  <React.StrictMode>
    <ReactKeycloakProvider
      authClient={keycloak}
      onEvent={eventLogger}
      onTokens={tokenLogger}
      initOptions={init}
    >
      <App />
    </ReactKeycloakProvider>
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
