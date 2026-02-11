package org.commonprovenance.framework.store.web.trustedParty;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CertificateClient {
  @NotNull
  Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> trustedPartyUrl);

  @NotNull
  Mono<Boolean> updateOrganizationCertificate(
      @NotNull String organizationId,
      @NotNull String clientCertificate,
      @NotNull List<String> intermediateCertificates,
      Optional<String> trustedPartyUrl);
}
