package org.commonprovenance.framework.store.web.trustedParty;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CertificateClient {
  @NotNull
  Mono<Organization> getOrganizationCertificate(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl);

  @NotNull
  Mono<Boolean> updateOrganizationCertificate(
      @NotNull String organizationId,
      @NotNull String clientCertificate,
      @NotNull List<String> intermediateCertificates,
      Optional<String> trustedPartyUrl);
}
