package org.commonprovenance.framework.store.service.persistence.metaComponent;

import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Mono;

public interface MetaProvenanceComponentService {

  Mono<Void> createMetaProvenanceComponentIfNotExists(Document document);

  Mono<Void> addBundleVersionIntoMetaProvenanceComponent(Document document);

  Mono<Void> addTokenIntoMetaProvenanceComponent(Document document);

  Mono<Boolean> metaProvenanceComponentExists(String metaBundleIdentifier);

  Mono<Boolean> metaProvenanceComponentNotExists(String metaBundleIdentifier);

  Mono<org.openprovenance.prov.model.Document> getMetaProvenanceComponent(String metaBundleIdentifier);
}
