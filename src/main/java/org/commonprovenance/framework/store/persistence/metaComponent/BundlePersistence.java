package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.function.Function;

import org.openprovenance.prov.model.Document;

import reactor.core.publisher.Mono;

public interface BundlePersistence {

  Mono<Void> create(String identifier);

  Function<String, Mono<Void>> addVersionEntity(String identifier);

  Function<String, Mono<Void>> addToken(String identifier);

  Mono<Document> getByIdentifier(String identifier);

  Mono<Boolean> exists(String identifier);

  Mono<Boolean> notExists(String identifier);

}
