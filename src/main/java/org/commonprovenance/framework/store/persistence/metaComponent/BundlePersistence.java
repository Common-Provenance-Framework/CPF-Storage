package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.Document;

import reactor.core.publisher.Mono;

public interface BundlePersistence {

  Mono<Document> create(String identifier);

  Function<String, Mono<Document>> createFirstVersion(String identifier);

  Function<String, Mono<Document>> createVersion(String identifier);

  Function<String, Mono<Document>> createNewVersion(String identifier);

  Function<Token, Mono<Document>> createToken(String identifier);

  Mono<Document> getByIdentifier(String identifier);

  Mono<Boolean> exists(String identifier);

  Mono<Boolean> notExists(String identifier);

}
