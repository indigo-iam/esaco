package it.infn.mw.esaco.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.infn.mw.esaco.EsacoProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth,
    EsacoProperties properties)
    throws Exception {

    PasswordEncoder encoder = PasswordEncoderFactories
      .createDelegatingPasswordEncoder();

    auth.inMemoryAuthentication()
      .withUser(properties.getUsername())
      .password(encoder.encode(properties
        .getPassword()))
      .roles("USER");

  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.authorizeRequests()
      .anyRequest()
      .authenticated()
      .and()
      .httpBasic()
      .and()
      .csrf()
      .disable();

  }

}
