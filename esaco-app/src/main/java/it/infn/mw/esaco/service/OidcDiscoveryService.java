package it.infn.mw.esaco.service;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

public interface OidcDiscoveryService {

  public JsonNode getDiscoveryDocument(String issuer, RestTemplate restTemplate)
      throws RestClientException;

}
