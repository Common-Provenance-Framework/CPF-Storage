package org.commonprovenance.framework.store.support;

import org.commonprovenance.framework.store.support.trustedParty.TrustedPartyStub;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WireMockInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final WireMockServer WIRE_MOCK = new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static {
    WIRE_MOCK.start();
  }

  public static WireMockServer server() {
    return WIRE_MOCK;
  }

  public static String trustedPartyUrl() {
    return WIRE_MOCK.baseUrl() + "/api/v1/";
  }

  public static String otherStoreUrl() {
    return WIRE_MOCK.baseUrl() + "/other-store/api/v1/";
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    new TrustedPartyStub(WIRE_MOCK).providesItsInfo();
    TestPropertyValues.of("trusted-party.url=" + trustedPartyUrl())
        .applyTo(applicationContext.getEnvironment());
    applicationContext.getBeanFactory().registerSingleton("wireMockServer", WIRE_MOCK);
  }
}
