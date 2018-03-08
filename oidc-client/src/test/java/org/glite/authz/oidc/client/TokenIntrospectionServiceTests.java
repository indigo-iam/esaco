package org.glite.authz.oidc.client;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.UnsupportedIssuerException;
import org.glite.authz.oidc.client.service.TokenIntrospectionService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenIntrospectionServiceTests extends ClientTestUtils {

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private RestTemplate restTemplate;

  @Autowired
  private TokenIntrospectionService tokenIntrospectionService;

  @Test
  public void testPostWithBasicAuthN() throws Exception {

    String expected = mapper.writeValueAsString(VALID_INTROSPECTION);

    when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .thenReturn(expected);

    Optional<String> response = tokenIntrospectionService.introspectToken(VALID_JWT);

    assertThat(response.isPresent(), is(true));
    assertThat(response.get(), not(isEmptyOrNullString()));
    assertThat(response.get(), equalTo(expected));
  }

  @Test
  public void testPostWithOAuth() throws Exception {

    String expected = mapper.writeValueAsString(VALID_USERINFO);

    when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .thenReturn(expected);

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);

    assertThat(response.isPresent(), is(true));
    assertThat(response.get(), not(isEmptyOrNullString()));
    assertThat(response.get(), equalTo(expected));
  }

  @Test(expected = HttpConnectionException.class)
  public void testConnectionError() {

    when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .thenThrow(new RestClientException(""));

    try {
      tokenIntrospectionService.introspectToken(VALID_JWT);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test
  public void testUnauthorizedUserinfo() {
    given(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
      .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);
    assertThat(response.isPresent(), is(false));
  }

  @Test
  public void testForbiddenUserinfo() {
    when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);
    assertThat(response.isPresent(), is(false));
  }

  @Test(expected = UnsupportedIssuerException.class)
  public void testIntrospectionWithUnsupportedIssuer() {
    try {
      tokenIntrospectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = UnsupportedIssuerException.class)
  public void testuserInfoWithUnsupportedIssuer() {

    try {
      tokenIntrospectionService.getUserInfoForToken(TOKEN_FROM_UNKNOWN_ISSUER);
    } catch (Exception e) {
      throw e;
    }
  }

}
