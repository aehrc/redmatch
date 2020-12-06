/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * A {@link Converter} that maps Keycloak realm roles to Spring {@link GrantedAuthority} objects. 
 * 
 * @author Alejandro Metke
 *
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  @SuppressWarnings("unchecked")
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    final Map<String, Object> realmAccess = 
        (Map<String, Object>) jwt.getClaims().get("realm_access");
    return ((List<String>)realmAccess.get("roles")).stream()
        .map(roleName -> "ROLE_" + roleName)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
