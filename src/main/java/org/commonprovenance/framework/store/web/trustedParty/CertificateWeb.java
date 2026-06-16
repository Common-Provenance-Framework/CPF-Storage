package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface CertificateWeb {

  Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> optTrustedPartyBaseUrl);

  Mono<Void> updateOrganizationCertificate(Organization organization);
}
