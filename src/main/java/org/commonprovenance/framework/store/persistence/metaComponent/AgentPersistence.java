package org.commonprovenance.framework.store.persistence.metaComponent;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;

import reactor.core.publisher.Mono;

public interface AgentPersistence {

  Mono<AgentNode> create(AgentNode agent);

  Mono<AgentNode> getByIdentifier(String identifier);

}
