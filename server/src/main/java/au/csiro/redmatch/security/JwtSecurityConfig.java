/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;


/**
 * Configures Redmatch as an OAuth2 resource server that accepts JWT tokens.
 * 
 * @author Alejandro Metke
 *
 */
@Configuration
@ConditionalOnProperty(value = "redmatch.security.enabled", havingValue = "true")
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
    .and()
    .authorizeRequests()
    .antMatchers("/**")
    .hasRole("user")
    .and()
    .oauth2ResourceServer(
      oauth2ResourceServer -> oauth2ResourceServer.jwt(
        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
      )
    );
  }
  
  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
    return jwtConverter;
  }
}