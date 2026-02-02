package org.commonprovenance.framework.store.persistence.entity.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;

import reactor.core.publisher.Mono;

public class EntityFactory {
  private static DocumentEntity fromModel(Document model) {
    return new DocumentEntity(
        model.getId().toString(),
        model.getGraph(),
        model.getFormat().toString());
  }

  // ---

  public static Mono<DocumentEntity> toEntity(Document document) {
    return MONO.makeSureNotNull(document)
        .map(EntityFactory::fromModel);
  }
}
