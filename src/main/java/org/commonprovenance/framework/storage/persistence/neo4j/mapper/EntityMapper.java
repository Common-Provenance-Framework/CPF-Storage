package org.commonprovenance.framework.storage.persistence.neo4j.mapper;

import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.persistence.neo4j.entities.DocumentEntity;
import reactor.core.publisher.Mono;

public class EntityMapper {
  public static Mono<DocumentEntity> toEntity(Document domain) {
    return Mono.just(new DocumentEntity(domain.getIdentifier().toString(), domain.getGraph(), domain.getFormat().toString()));
  }
}
