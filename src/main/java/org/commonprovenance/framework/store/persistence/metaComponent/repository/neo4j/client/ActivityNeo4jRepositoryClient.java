package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityNeo4jRepositoryClient extends ReactiveNeo4jRepository<ActivityNode, String> {
}
