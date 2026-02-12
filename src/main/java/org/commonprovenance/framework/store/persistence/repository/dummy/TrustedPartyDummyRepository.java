package org.commonprovenance.framework.store.persistence.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.commonprovenance.framework.store.persistence.repository.TrustedPartyRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class TrustedPartyDummyRepository implements TrustedPartyRepository {
  private static Map<String, TrustedPartyEntity> trustedParties = new HashMap<>();

  private void add(TrustedPartyEntity entity) {
    trustedParties.put(entity.getId(), entity);
  }

  @Override
  public Mono<TrustedPartyEntity> save(TrustedPartyEntity entity) {
    return MONO.makeSureNotNull(entity)
        .doOnNext(this::add);
  }

  @Override
  public Flux<TrustedPartyEntity> findAll() {
    return Flux.fromIterable(trustedParties.values());
  }

  @Override
  public Mono<TrustedPartyEntity> findById(String id) {
    return MONO.makeSureNotNull(id)
        .map(trustedParties::get)
        .flatMap(MONO::makeSureNotNull);
  }

  @Override
  public Mono<TrustedPartyEntity> findByName(String name) {
    return MONO.makeSureNotNull(name)
        .thenMany(this.findAll())
        .filter(tp -> tp.getName().equals(name))
        .singleOrEmpty()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty with name '" + name + "' not found!")));
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return MONO.makeSureNotNull(id)
        .map(trustedParties::remove)
        .then();
  }

  @Override
  public Mono<TrustedPartyEntity> findDefault() {
    return this.findAll()
        .filter(TrustedPartyEntity::getIsDefault)
        .singleOrEmpty()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty default not found!")));

  }
}
