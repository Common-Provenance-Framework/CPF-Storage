package org.commonprovenance.framework.store.persistence.repository;

import org.commonprovenance.framework.store.persistence.entity.TokenEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository {
  Mono<TokenEntity> save(TokenEntity entity);

  Flux<TokenEntity> findAll();

  Mono<TokenEntity> findById(String id);

  Mono<Void> deleteById(String id);
}
