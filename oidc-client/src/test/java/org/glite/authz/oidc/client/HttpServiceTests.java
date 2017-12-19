package org.glite.authz.oidc.client;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.service.HttpService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class})
@SpringBootTest
public class HttpServiceTests extends ClientTestUtils {

  @Autowired
  ObjectMapper mapper;

  @Autowired
  private HttpService httpService;

  @MockBean
  private RestTemplate restTemplate;

  @Test
  public void testPostWithBasicAuthN() throws Exception {

    String expected = mapper.writeValueAsString(VALID_INTROSPECTION);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(0);
    body.add("token", VALID_JWT);

    given(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .willReturn(expected);

    String response =
        httpService.postWithBasicAuthentication("/introspect", "admin", "password", body);

    assertNotNull(response);
    assertThat(response, not(isEmptyOrNullString()));
    assertThat(response, equalTo(expected));
  }

  @Test
  public void testPostWithOAuth() throws Exception {

    String expected = mapper.writeValueAsString(VALID_USERINFO);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(0);

    given(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .willReturn(expected);

    String response = httpService.postWithOAuthAuthentication("/userinfo", VALID_JWT, body);

    assertNotNull(response);
    assertThat(response, not(isEmptyOrNullString()));
    assertThat(response, equalTo(expected));
  }

  @Test(expected = HttpConnectionException.class)
  public void testConnectionError() {

    given(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
      .willThrow(new RestClientException(""));

    try {
      httpService.postWithBasicAuthentication("foo", "user", "pass", new LinkedMultiValueMap<>(0));
    } catch (Exception e) {
      throw e;
    }
  }

  @Test
  public void testUnauthorizedUserinfo() {
    given(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
      .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(0);
    String response = httpService.postWithOAuthAuthentication("/userinfo", VALID_JWT, body);
    assertNull(response);
  }

  @Test
  public void testForbiddenUserinfo() {
    given(httpService.postWithOAuthAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.any())).willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(0);
    String response = httpService.postWithOAuthAuthentication("/userinfo", VALID_JWT, body);
    assertNull(response);
  }

}
