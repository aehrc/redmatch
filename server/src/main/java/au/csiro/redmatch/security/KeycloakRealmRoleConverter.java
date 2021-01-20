/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * A {@link Converter} that maps Keycloak realm and resource roles to Spring 
 * {@link GrantedAuthority} objects. 
 * 
 * @author Alejandro Metke
 *
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(KeycloakRealmRoleConverter.class);
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    final List<GrantedAuthority> res = new ArrayList<>();
    final Map<String, Object> claims = jwt.getClaims();
    
    // Add roles that come in the realm access section of the JWT token
    final Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
    log.debug("Extracting roles from realm_access");
    if (realmAccess != null) {
      final List<String> roles = ((List<String>) realmAccess.get("roles"));
      if (roles != null) {
        List<GrantedAuthority> gas = roles.stream()
            .map(roleName -> "ROLE_" + roleName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        log.debug("Found the following roles: " + gas);
        res.addAll(gas);
      }
    } else {
      log.debug("There are no roles in realm_access");
    }
    
    // Add roles that come in the resource access section of the JWT token
    log.debug("Extracting roles from resource_access");
    final Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
    if (resourceAccess != null) {
      // Extract all the nested claims - roles should be inside
      final List<Object> resourceAccessEntries = resourceAccess.entrySet().stream()
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
      
      log.debug("Found " + resourceAccessEntries.size() + " resources");
      
      for (Object resourceAccessEntry : resourceAccessEntries) {
        Map<String, Object> entry = (Map<String, Object>) resourceAccessEntry;
        final List<String> roles = ((List<String>) entry.get("roles"));
        if (roles != null) {
          List<GrantedAuthority> gas = roles.stream()
              .map(roleName -> "ROLE_" + roleName)
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());
          log.debug("Found the following roles: " + gas);
          res.addAll(gas);
        }
      }
      
    } else {
      log.debug("There are no roles in resource_access");
    }
    return res;
  }
}
