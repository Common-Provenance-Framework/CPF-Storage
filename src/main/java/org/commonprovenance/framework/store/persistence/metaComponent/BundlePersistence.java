package org.commonprovenance.framework.store.persistence.metaComponent;

import org.openprovenance.prov.model.Document;

import reactor.core.publisher.Mono;

public interface BundlePersistence {

  Mono<Document> create(Document bundle);

  Mono<Document> getByIdentifier(String identifier);

  Mono<Boolean> exists(String identifier);

}
