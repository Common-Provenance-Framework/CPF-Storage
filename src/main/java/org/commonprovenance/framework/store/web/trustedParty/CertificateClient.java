package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface CertificateClient {

  Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> trustedPartyUrl);

  Function<Organization, Mono<Organization>> updateOrganizationCertificate(Optional<String> trustedPartyUrl);
}
