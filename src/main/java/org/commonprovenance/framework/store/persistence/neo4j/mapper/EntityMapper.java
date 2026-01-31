package org.commonprovenance.framework.store.persistence.neo4j.mapper;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.neo4j.entities.DocumentEntity;

import reactor.core.publisher.Mono;

public class EntityMapper {
  public static Mono<DocumentEntity> toEntity(Document domain) {
    return Mono
        .just(new DocumentEntity(domain.getId().toString(), domain.getGraph(), domain.getFormat().toString()));
  }
}
