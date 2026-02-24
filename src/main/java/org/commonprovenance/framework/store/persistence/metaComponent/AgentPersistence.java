package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.UUID;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface AgentPersistence {
  @NotNull
  Mono<AgentNode> create(@NotNull AgentNode agent);

  @NotNull
  Mono<AgentNode> getById(@NotNull UUID id);

}
