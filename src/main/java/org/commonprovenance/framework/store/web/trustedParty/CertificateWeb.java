package org.commonprovenance.framework.store.web.trustedParty;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface CertificateWeb {

  Mono<Void> updateOrganizationCertificate(Organization organization);
}
