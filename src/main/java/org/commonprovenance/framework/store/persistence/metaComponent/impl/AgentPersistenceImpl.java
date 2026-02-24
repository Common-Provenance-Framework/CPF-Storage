package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

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
  public Mono<AgentNode> getById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("Agent Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("AgentNeo4jRepository - Error while reading agent"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Agent with id '" + uuid.toString() + "' has not been found!"))));
  }

}
