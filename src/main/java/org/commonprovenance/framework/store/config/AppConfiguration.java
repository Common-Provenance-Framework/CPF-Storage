package org.commonprovenance.framework.store.config;

public class AppConfiguration {
  private final String fqdn;
  private final boolean verboseMode;
  private final boolean nroEnable;

  AppConfiguration(String fqdn, boolean verboseMode, boolean nroEnable) {
    this.fqdn = fqdn;
    this.verboseMode = verboseMode;
    this.nroEnable = nroEnable;
  }

  AppConfiguration(String fqdn, boolean verboseMode) {
    this.fqdn = fqdn;
    this.verboseMode = verboseMode;
    this.nroEnable = true;
  }

  public String getFqdn() {
    return this.fqdn;
  }

  public boolean isVerboseMode() {
    return verboseMode;
  }

  public boolean isNROEnabled() {
    return nroEnable;
  }
}
