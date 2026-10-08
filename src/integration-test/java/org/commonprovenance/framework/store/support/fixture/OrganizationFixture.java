package org.commonprovenance.framework.store.support.fixture;

import java.util.List;
import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;

public final class OrganizationFixture {

  public static final String CLIENT_CERTIFICATE = "client-certificate";
  public static final List<String> INTERMEDIATE_CERTIFICATES = List.of("intermediate-certificate");

  private OrganizationFixture() {
  }

  public static Organization random(TrustedParty trustedParty) {
    return new Organization(
        UUID.randomUUID().toString(),
        CLIENT_CERTIFICATE,
        INTERMEDIATE_CERTIFICATES,
        trustedParty,
        null);
  }
}
