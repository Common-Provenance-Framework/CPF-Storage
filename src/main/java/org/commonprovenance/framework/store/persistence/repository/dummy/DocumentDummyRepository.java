package org.commonprovenance.framework.store.persistence.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.repository.DocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class DocumentDummyRepository implements DocumentRepository {
  private static Map<String, DocumentEntity> documents = new HashMap<>();

  private void add(DocumentEntity entity) {
    documents.put(entity.getId(), entity);
  }

  @Override
  public Mono<DocumentEntity> save(DocumentEntity entity) {
    return MONO.makeSureNotNull(entity)
        .doOnNext(this::add);
  }

  @Override
  public Flux<DocumentEntity> findAll() {
    return Flux.fromIterable(documents.values());
  }

  @Override
  public Mono<DocumentEntity> findById(String id) {
    return MONO.makeSureNotNull(id)
        .map(documents::get)
        .flatMap(MONO::makeSureNotNull);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return MONO.makeSureNotNull(id)
        .map(documents::remove)
        .then();
  }
}
