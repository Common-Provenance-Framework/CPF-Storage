package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.ActivityPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.ActivityRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class ActivityPersistenceImpl implements ActivityPersistence {

  private final ActivityRepository repository;

  public ActivityPersistenceImpl(
      ActivityRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<ActivityNode> create(@NotNull ActivityNode bundle) {
    return MONO.<ActivityNode>makeSureNotNullWithMessage("Activity can not be 'null'!").apply(bundle)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("ActivityNeo4jRepository - Error while creating new Activity"));
  }

  @Override
  @NotNull
  public Mono<ActivityNode> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Activity Id can not be 'null'!").apply(id)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("ActivityNeo4jRepository - Error while reading activity"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Activity with id '" + id + "' has not been found!"))));
  }

}
