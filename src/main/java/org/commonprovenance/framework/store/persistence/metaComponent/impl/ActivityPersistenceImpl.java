package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.ActivityPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.ActivityRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class ActivityPersistenceImpl implements ActivityPersistence {

  private final ActivityRepository repository;

  public ActivityPersistenceImpl(
      ActivityRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<ActivityNode> create(ActivityNode activity) {
    return MONO.<ActivityNode>makeSureNotNullWithMessage("Activity can not be 'null'!").apply(activity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("ActivityPersistence - Error while creating new Activity"));
  }

  @Override
  public Mono<ActivityNode> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Activity identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("ActivityPersistence - Error while reading activity"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Activity with identifierÍ '" + identifier + "' has not been found!"))));
  }

}
