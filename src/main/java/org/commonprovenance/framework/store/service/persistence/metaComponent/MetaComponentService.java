package org.commonprovenance.framework.store.service.persistence.metaComponent;

import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Mono;

public interface MetaComponentService {

  Mono<Void> createMetaComponent(Document document);

  Mono<Void> addNewVersion(Document document);

  Mono<Void> addTokenToLastVersion(Document document);

  Mono<Boolean> exists(String identifier);
}
