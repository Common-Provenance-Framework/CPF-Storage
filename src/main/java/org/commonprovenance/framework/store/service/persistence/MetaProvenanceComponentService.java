package org.commonprovenance.framework.store.service.persistence;

import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface MetaProvenanceComponentService {

  Mono<Void> createMetaProvenanceComponentIfNotExists(Organization organization);

  Mono<Void> addBundleVersionIntoMetaProvenanceComponent(Organization organization);

  Mono<Void> addTokenIntoMetaProvenanceComponent(Organization organization);

  Mono<Boolean> metaProvenanceComponentExists(String metaBundleIdentifier);

  Mono<MetaDocument> getMetaProvenanceComponent(String metaBundleIdentifier);
}
