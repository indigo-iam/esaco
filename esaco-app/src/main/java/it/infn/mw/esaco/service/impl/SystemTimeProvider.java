package it.infn.mw.esaco.service.impl;

import java.util.Calendar;

import org.springframework.stereotype.Component;

import it.infn.mw.esaco.service.TimeProvider;

@Component
public class SystemTimeProvider implements TimeProvider {

  @Override
  public long currentTimeMillis() {

    return Calendar.getInstance().getTimeInMillis();
  }
}
