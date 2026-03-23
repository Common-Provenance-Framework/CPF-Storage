package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class DocumentDummyRepository implements DocumentRepository {
  private static Map<String, DocumentNode> documents = new HashMap<>();

  private void add(DocumentNode entity) {
    documents.put(entity.getIdentifier(), entity);
  }

  @Override
  public Mono<DocumentNode> save(DocumentNode entity) {
    return MONO.makeSureNotNull(entity)
        .doOnNext(this::add);
  }

  @Override
  public Flux<DocumentNode> findAll() {
    return Flux.fromIterable(documents.values());
  }

  @Override
  public Mono<DocumentNode> findByIdentifier(String identifier) {
    return MONO.makeSureNotNull(identifier)
        .map(documents::get)
        .flatMap(MONO::makeSureNotNull);
  }

  @Override
  public Mono<Void> deleteByIdentifier(String identifier) {
    return MONO.makeSureNotNull(identifier)
        .map(documents::remove)
        .then();
  }
}
