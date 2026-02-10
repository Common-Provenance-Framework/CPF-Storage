package org.commonprovenance.framework.store.web.trustedParty;

import java.util.List;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CertificateClient {
  @NotNull
  Mono<Organization> getOrganizationCertificate(@NotNull String id);

  @NotNull
  Mono<Boolean> updateOrganizationCertificate(
      @NotNull String id,
      @NotNull String clientCertificate,
      @NotNull List<String> intermediateCertificates);
}
