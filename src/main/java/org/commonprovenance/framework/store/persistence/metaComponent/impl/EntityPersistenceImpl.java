package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class EntityPersistenceImpl implements EntityPersistence {

  private final EntityRepository repository;

  public EntityPersistenceImpl(
      EntityRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<EntityNode> create(@NotNull EntityNode entity) {
    return MONO.<EntityNode>makeSureNotNullWithMessage("Entity can not be 'null'!").apply(entity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("EntityNeo4jRepository - Error while creating new Entity"));
  }

  @Override
  @NotNull
  public Mono<EntityNode> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Entity Id can not be 'null'!").apply(id)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("EntityNeo4jRepository - Error while reading entity"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Entity with id '" + id + "' has not been found!"))));
  }

}
