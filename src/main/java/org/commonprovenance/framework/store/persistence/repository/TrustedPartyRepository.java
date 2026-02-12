package org.commonprovenance.framework.store.persistence.repository;

import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrustedPartyRepository {
  Mono<TrustedPartyEntity> save(TrustedPartyEntity entity);

  Flux<TrustedPartyEntity> findAll();

  Mono<TrustedPartyEntity> findById(String id);

  Mono<TrustedPartyEntity> findByName(String name);

  Mono<Void> deleteById(String id);
}
