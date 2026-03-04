package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;

import reactor.core.publisher.Mono;

public interface AgentRepository {
  Mono<AgentNode> save(AgentNode agent);

  Mono<AgentNode> findById(String id);
}
