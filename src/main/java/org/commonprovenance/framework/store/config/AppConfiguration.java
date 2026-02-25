package org.commonprovenance.framework.store.config;

public class AppConfiguration {
  private final String fqdn;

  AppConfiguration(String fqdn) {
    this.fqdn = fqdn;
  }

  public String getFqdn() {
    return this.fqdn;
  }
}
