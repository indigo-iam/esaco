package it.infn.mw.esaco.test;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.test.utils.TestConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EsacoApplication.class, TestConfig.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ActuatorTests {

  public static final String HEALTH_ENDPOINT = "/actuator/health";
  public static final String INFO_ENDPOINT = "/actuator/info";

  @Autowired
  private MockMvc mvc;

  @Test
  @WithAnonymousUser
  public void healthEndpointDoesNotRequireAuthentication() throws Exception {
    mvc.perform(get(HEALTH_ENDPOINT)).andExpect(unauthenticated()).andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  public void infoEndpointDoesNotRequireAuthentication() throws Exception {
    mvc.perform(get(INFO_ENDPOINT)).andExpect(unauthenticated()).andExpect(status().isOk());
  }


}
