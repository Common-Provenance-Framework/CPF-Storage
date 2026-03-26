package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.function.Function;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Entity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityPersistence {

  Mono<Entity> create(Entity entity);

  Function<Entity, Mono<Entity>> addFirstVersion(Entity general);

  Function<Entity, Mono<Entity>> addNewVersion(Entity general, Entity lastVersion);

  Function<Entity, Mono<Entity>> addToken(Entity token, Activity generation, Agent generator);

  Mono<Entity> getByIdentifier(String identifier);

  Flux<Entity> getAllEntities(String bundleIdentifier);

  Mono<Entity> getGeneralVersionEntity(String bundleIdentifier);

  Mono<Entity> getLastVersionEntity(String bundleIdentifier);

  Mono<Integer> getLastVersion(String bundleIdentifier);

}
