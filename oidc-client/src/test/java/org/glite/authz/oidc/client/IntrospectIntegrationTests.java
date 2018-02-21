package org.glite.authz.oidc.client;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.glite.authz.oidc.client.service.TokenInfoService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username="test", roles="USER")
public class IntrospectIntegrationTests  extends ClientTestUtils{
  
  final static String ENDPOINT = "/introspect";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;
  
  @MockBean
  TokenInfoService tokenInfoService;
  
  @Test
  @WithAnonymousUser
  public void introspectEndpointRequiresAuthenticatedUser() throws Exception {
    mvc.perform(post(ENDPOINT))
    .andDo(print())
    .andExpect(status().isUnauthorized());
  }
  
  @Test
  public void testIntrospectWithoutToken() throws Exception {

    mvc.perform(post(ENDPOINT))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())));
  }
  
  @Test
  public void testUnreadableToken() throws Exception {
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
  public void testIntrospectionWithInvalidToken() throws Exception {
    
    when(tokenInfoService.isAccessTokenActive(Mockito.any())).thenReturn(false);
    
    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value("false"));
    
    
  }
  
  @Test
  public void testIntrospectionWithValidToken() throws Exception {
    
    when(tokenInfoService.isAccessTokenActive(Mockito.any())).thenReturn(true);
    when(tokenInfoService.introspectToken(VALID_JWT)).thenReturn(VALID_INTROSPECTION);
    
    mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value("true"))
        .andExpect(jsonPath("$.iss").value(ISS))
        .andExpect(jsonPath("$.sub").value("73f16d93-2441-4a50-88ff-85360d78c6b5"));
    
    verify(tokenInfoService).introspectToken(Mockito.eq(VALID_JWT));
        
  }
  
}
