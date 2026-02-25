package org.commonprovenance.framework.store.persistence.metaComponent;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface AgentPersistence {
  @NotNull
  Mono<AgentNode> create(@NotNull AgentNode agent);

  @NotNull
  Mono<AgentNode> getById(@NotNull String id);

}
