package org.commonprovenance.framework.store.support;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WireMockResetExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    WireMockInitializer.server().resetAll();
  }
}
