package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.AgentPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.AgentRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class AgentPersistenceImpl implements AgentPersistence {

  private final AgentRepository repository;

  public AgentPersistenceImpl(
      AgentRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<AgentNode> create(@NotNull AgentNode bundle) {
    return MONO.<AgentNode>makeSureNotNullWithMessage("Agent can not be 'null'!").apply(bundle)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("AgentNeo4jRepository - Error while creating new Agent"));
  }

  @Override
  @NotNull
  public Mono<AgentNode> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Agent Id can not be 'null'!").apply(id)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("AgentNeo4jRepository - Error while reading agent"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Agent with id '" + id + "' has not been found!"))));
  }

}
