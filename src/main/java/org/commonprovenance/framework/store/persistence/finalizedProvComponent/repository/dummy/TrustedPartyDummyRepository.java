package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TrustedPartyRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class TrustedPartyDummyRepository implements TrustedPartyRepository {
  private static Map<String, TrustedPartyNode> trustedParties = new HashMap<>();

  private void add(TrustedPartyNode entity) {
    trustedParties.put(entity.getId(), entity);
  }

  @Override
  public Mono<TrustedPartyNode> save(TrustedPartyNode trustedParty) {
    return MONO.makeSureNotNull(trustedParty)
        .doOnNext(this::add);
  }

  @Override
  public Flux<TrustedPartyNode> findAll() {
    return Flux.fromIterable(trustedParties.values());
  }

  @Override
  public Mono<TrustedPartyNode> findByName(String name) {
    return MONO.makeSureNotNull(name)
        .thenMany(this.findAll())
        .filter(tp -> tp.getName().equals(name))
        .singleOrEmpty()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty with name '" + name + "' not found!")));
  }

  @Override
  public Mono<TrustedPartyNode> findDefault() {
    return this.findAll()
        .filter(TrustedPartyNode::getIsDefault)
        .singleOrEmpty()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty default not found!")));

  }
}
