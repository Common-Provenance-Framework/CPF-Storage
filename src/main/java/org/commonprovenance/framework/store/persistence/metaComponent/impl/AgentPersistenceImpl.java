package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.AgentPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.AgentRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class AgentPersistenceImpl implements AgentPersistence {

  private final AgentRepository repository;

  public AgentPersistenceImpl(
      AgentRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<AgentNode> create(AgentNode agent) {
    return MONO.<AgentNode>makeSureNotNullWithMessage("Agent can not be 'null'!").apply(agent)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("AgentPersistence - Error while creating new Agent"));
  }

  @Override
  public Mono<AgentNode> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Agent identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("AgentPersistence - Error while reading agent"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Agent with identifier '" + identifier + "' has not been found!"))));
  }

}
