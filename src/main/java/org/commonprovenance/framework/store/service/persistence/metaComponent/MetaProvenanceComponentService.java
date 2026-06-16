package org.commonprovenance.framework.store.service.persistence.metaComponent;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface MetaProvenanceComponentService {

  Mono<Void> createMetaProvenanceComponentIfNotExists(Organization organization);

  Mono<Void> addBundleVersionIntoMetaProvenanceComponent(Organization organization);

  Mono<Void> addTokenIntoMetaProvenanceComponent(Organization organization);

  Mono<Boolean> metaProvenanceComponentExists(String metaBundleIdentifier);

  Mono<Boolean> metaProvenanceComponentNotExists(String metaBundleIdentifier);

  Mono<org.openprovenance.prov.model.Document> getMetaProvenanceComponent(String metaBundleIdentifier);
}
