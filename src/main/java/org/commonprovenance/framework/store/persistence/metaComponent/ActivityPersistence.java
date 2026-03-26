package org.commonprovenance.framework.store.persistence.metaComponent;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;

import reactor.core.publisher.Mono;

public interface ActivityPersistence {

  Mono<ActivityNode> create(ActivityNode activity);

  Mono<ActivityNode> getByIdentifier(String identifier);

}
