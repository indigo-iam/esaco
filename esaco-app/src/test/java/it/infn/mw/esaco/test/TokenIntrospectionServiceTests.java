package it.infn.mw.esaco.test;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.TokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;
import it.infn.mw.esaco.test.utils.TestConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EsacoApplication.class, TestConfig.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenIntrospectionServiceTests extends EsacoTestUtils {

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
    assertThat(response.get(), not(is(emptyOrNullString())));
    assertThat(response.get(), equalTo(expected));
  }

  @Test
  public void testPostWithOAuth() throws Exception {

    String expected = mapper.writeValueAsString(VALID_USERINFO);

    when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .thenReturn(expected);

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);

    assertThat(response.isPresent(), is(true));
    assertThat(response.get(), not(is(emptyOrNullString())));
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

  @Test
  public void testIntrospectionWithUnsupportedIssuer() {

    assertThat(tokenIntrospectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER).isEmpty(),
        is(true));

  }

  @Test(expected = UnsupportedIssuerException.class)
  public void testuserInfoWithUnsupportedIssuer() {


      tokenIntrospectionService.getUserInfoForToken(TOKEN_FROM_UNKNOWN_ISSUER);

  }

}
