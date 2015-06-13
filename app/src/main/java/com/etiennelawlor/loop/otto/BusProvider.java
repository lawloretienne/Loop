package com.etiennelawlor.loop.otto;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class BusProvider {

  private BusProvider() {
    // No instances.
  }

  public static Bus get() {
    return BUS;
  }

  private static final Bus BUS = new Bus(ThreadEnforcer.ANY);
}