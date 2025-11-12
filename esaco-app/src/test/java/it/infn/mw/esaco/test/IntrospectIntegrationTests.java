package it.infn.mw.esaco.test;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.TokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = "USER")
public class IntrospectIntegrationTests extends EsacoTestUtils {

  final static String ENDPOINT = "/introspect";

  @Autowired
  private MockMvc mvc;

  @Autowired
  TokenIntrospectionService tokenIntrospectionService;

  @MockitoBean
  OpaqueTokenIntrospector inspector;

  @Test
  @WithAnonymousUser
  void introspectEndpointRequiresAuthenticatedUser() throws Exception {
    mvc.perform(post(ENDPOINT)).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  void testIntrospectWithoutToken() throws Exception {

    mvc.perform(post(ENDPOINT))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.detail", equalTo("Required parameter 'token' is not present.")));
  }

  @Test
  void testMalformedToken() throws Exception {
    String token = "abcdefghilmnopqrstuvz";

    mvc.perform(post(ENDPOINT).param("token", token))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())))
      .andExpect(jsonPath("$.message", equalTo("Malformed JWT token string")));
  }

  @Test
  void testIntrospectionRaiseDiscoveryDocumentNotFoundError() throws Exception {

    String errorMessage = format("No introspection_endpoint in discovery document for %s", ISS);
    when(inspector.introspect(VALID_JWT))
      .thenThrow(new DiscoveryDocumentNotFoundException(errorMessage));

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isBadGateway())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(BAD_GATEWAY.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_GATEWAY.getReasonPhrase())))
      .andExpect(jsonPath("$.message", equalTo(errorMessage)));
  }

  @Test
  void testIntrospectionRaiseUnsupportedIssuerError() throws Exception {

    String errorMessage = format("Unsupported issuer: %s", ISS);
    when(inspector.introspect(VALID_JWT)).thenThrow(new UnsupportedIssuerException(errorMessage));

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())))
      .andExpect(jsonPath("$.message", equalTo(errorMessage)));
  }

  @Test
  void testIntrospectionRaiseHttpConnectionError() throws Exception {

    String errorMessage = "Connection error";
    when(inspector.introspect(VALID_JWT)).thenThrow(new HttpConnectionException(errorMessage));

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(INTERNAL_SERVER_ERROR.value())))
      .andExpect(jsonPath("$.error", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase())))
      .andExpect(jsonPath("$.message", equalTo(errorMessage)));
  }

  @Test
  void testIntrospectionRaiseHttpClientUnauthorizedError() throws Exception {

    when(inspector.introspect(VALID_JWT)).thenThrow(HttpClientErrorException
      .create(HttpStatusCode.valueOf(401), "Unauthorized error", null, null, null));

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(UNAUTHORIZED.value())))
      .andExpect(jsonPath("$.error", equalTo(UNAUTHORIZED.getReasonPhrase())));
  }

  // HttpClientErrorException.Unauthorized
  @Test
  void testIntrospectionWithExpiredToken() throws Exception {

    when(inspector.introspect(VALID_JWT)).thenReturn(EXPIRED_INTROSPECTION);

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.active").value("false"));
  }

  @Test
  void testIntrospectionWithExtraInformationValidToken() throws Exception {

    when(inspector.introspect(EXTRA_INFORMATION_JWT)).thenReturn(EXTRA_INFORMATION_INTROSPECTION);

    mvc.perform(post(ENDPOINT).param("token", EXTRA_INFORMATION_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.unecessary_field").value("unecessary_information"));
  }

  @Test
  void testIntrospectionWithValidToken() throws Exception {

    when(inspector.introspect(VALID_JWT)).thenReturn(VALID_INTROSPECTION);

    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.active").value("true"))
      .andExpect(jsonPath("$.iss").value(ISS))
      .andExpect(jsonPath("$.sub").value("73f16d93-2441-4a50-88ff-85360d78c6b5"))
      .andExpect(jsonPath("$.preferred_username").value("admin"))
      .andExpect(jsonPath("$.organisation_name").value("indigo-dc"))
      .andExpect(jsonPath("$.email").value("admin@example.org"))
      .andExpect(jsonPath("$.groups", hasItems("Production", "Analysis")))
      .andExpect(jsonPath("$.token_type", is("Bearer")))
      .andExpect(jsonPath("$.expires_at").exists())
      .andExpect(jsonPath("$.eduperson_entitlement",
          hasItems("urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu")))
      .andExpect(jsonPath("$.eduperson_entitlement",
          hasItems("urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu")))
      .andExpect(jsonPath("$.acr").value("https://aai.egi.eu/LoA#Substantial"));
  }
}
