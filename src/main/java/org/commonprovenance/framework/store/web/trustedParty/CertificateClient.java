package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CertificateClient {
  @NotNull
  Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> trustedPartyUrl);

  @NotNull
  Function<Organization, Mono<Organization>> updateOrganizationCertificate(Optional<String> trustedPartyUrl);
}
