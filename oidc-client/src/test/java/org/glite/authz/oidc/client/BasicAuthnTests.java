package org.glite.authz.oidc.client;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
@AutoConfigureMockMvc
public class BasicAuthnTests {

  public static final String INTROSPECT_ENDPOINT = "/introspect";
  public static final String TOKENINFO_ENDPOINT = "/tokeninfo";
  
  @Value("${security.user.name}")
  String username;
  
  @Value("${security.user.password}")
  String password;
  
  @Autowired
  private MockMvc mvc;
  
  @Test
  public void introspectEndpointRequiresAuthenticatedUser() throws Exception {
    mvc.perform(post(INTROSPECT_ENDPOINT))
    .andDo(print())
    .andExpect(unauthenticated())
    .andExpect(status().isUnauthorized());
  }
  
  @Test
  public void tokenInfoEndpointRequiresAuthenticatedUser() throws Exception {
    mvc.perform(post(TOKENINFO_ENDPOINT))
    .andDo(print())
    .andExpect(unauthenticated())
    .andExpect(status().isUnauthorized());
  }
  
  @Test
  public void checkAuthnIntrospectEndpoint() throws Exception {
    mvc.perform(post(INTROSPECT_ENDPOINT).with(httpBasic(username, password)))
    .andDo(print())
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void checkAuthnTokenInfoEndpoint() throws Exception {
    mvc.perform(post(TOKENINFO_ENDPOINT).with(httpBasic(username, password)))
    .andDo(print())
    .andExpect(status().isBadRequest());
  }

}
