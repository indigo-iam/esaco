package it.infn.mw.esaco.test.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TokenInfoServiceTestConfig {
  @Bean
  @Primary
  @Qualifier("mockTimeProvider")
  MockTimeProvider mockTimeProvider() {
    return new MockTimeProvider();
  }
}
