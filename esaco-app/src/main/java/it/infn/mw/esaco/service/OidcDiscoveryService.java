/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco.service;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;

public interface OidcDiscoveryService {

  public JsonNode getDiscoveryDocument(String issuer, RestTemplate restTemplate)
      throws RestClientException;

}
