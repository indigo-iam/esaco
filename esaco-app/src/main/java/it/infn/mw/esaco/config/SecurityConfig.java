package it.infn.mw.esaco.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import it.infn.mw.esaco.EsacoProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final OpaqueTokenIntrospector introspector;

  public SecurityConfig(OpaqueTokenIntrospector introspector) {
    this.introspector = introspector;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth.requestMatchers(EndpointRequest.to("health", "info"))
        .permitAll()
        .anyRequest()
        .authenticated())
      .httpBasic(Customizer.withDefaults())
      .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token.introspector(introspector)))
      .csrf(csrf -> csrf.disable());

    return http.build();
  }

  @Bean
  UserDetailsService userDetailsService(EsacoProperties properties) {
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    return new InMemoryUserDetailsManager(User.builder()
      .username(properties.getUsername())
      .password(encoder.encode(properties.getPassword()))
      .roles("USER")
      .build());
  }

}
