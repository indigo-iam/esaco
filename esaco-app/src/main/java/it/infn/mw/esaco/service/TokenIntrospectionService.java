package it.infn.mw.esaco.service;

import java.util.Optional;

import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.InvalidClientCredentialsException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;

public interface TokenIntrospectionService {

  Optional<String> introspect(String accessToken)
      throws DiscoveryDocumentNotFoundException, InvalidClientCredentialsException,
      TokenValidationException, UnsupportedIssuerException;

}
