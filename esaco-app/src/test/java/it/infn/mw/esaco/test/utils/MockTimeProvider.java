package it.infn.mw.esaco.test.utils;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import it.infn.mw.esaco.service.TimeProvider;

@Component
@Primary
public class MockTimeProvider implements TimeProvider {

  private long currentTimeMillis = System.currentTimeMillis();

  @Override
  public long currentTimeMillis() {
    return currentTimeMillis;
  }

  public void setTime(long timeMillis) {
    currentTimeMillis = timeMillis;
  }

}
