package it.infn.mw.esaco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableAutoConfiguration
@SpringBootApplication
@EnableCaching
public class EsacoApplication {

  public static void main(String[] args) {

    SpringApplication.run(EsacoApplication.class, args);
  }
}
