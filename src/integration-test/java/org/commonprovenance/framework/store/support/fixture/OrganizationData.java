package org.commonprovenance.framework.store.support.fixture;

import java.util.List;
import java.util.UUID;

public record OrganizationData(String id, String clientCertificate, List<String> intermediateCertificates) {

  public static OrganizationData newOrganization() {
    return new OrganizationData(
        UUID.randomUUID().toString(),
        "client-certificate",
        List.of("intermediate-certificate-2", "intermediate-certificate-1"));
  }

  public OrganizationData withRenewedCertificates() {
    return new OrganizationData(
        id,
        "renewed-client-certificate",
        List.of("renewed-intermediate-certificate"));
  }
}
