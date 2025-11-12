package it.infn.mw.esaco.service;

import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.model.IntrospectionResponse;

public interface TokenIntrospectionService {

  IntrospectionResponse introspect(String accessToken)
      throws TokenIntrospectionException;

}
