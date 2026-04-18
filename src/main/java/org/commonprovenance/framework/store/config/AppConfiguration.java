package org.commonprovenance.framework.store.config;

public class AppConfiguration {
  private final String fqdn;
  private final boolean verboseMode;

  AppConfiguration(String fqdn, boolean verboseMode) {
    this.fqdn = fqdn;
    this.verboseMode = verboseMode;
  }

  public String getFqdn() {
    return this.fqdn;
  }

  public boolean isVerboseMode() {
    return verboseMode;
  }
}
