/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import * as React from 'react'
import { Route, Redirect, RouteProps } from 'react-router-dom'

import { useKeycloak } from '@react-keycloak/web'

const PrivateRoute : React.FunctionComponent<RouteProps> = props => {
  const { keycloak } = useKeycloak();
  return (
    (() => {
      console.log('keycloak auth: ' + keycloak?.authenticated);
      if(keycloak?.authenticated) {
        return <Route {... props} />;
      } else {
        keycloak.login();
        return <Redirect
               to={{
                 pathname: '/',
                 state: { from: props.location },
               }}
             />;
      }
    })()
  )
};
export default PrivateRoute;