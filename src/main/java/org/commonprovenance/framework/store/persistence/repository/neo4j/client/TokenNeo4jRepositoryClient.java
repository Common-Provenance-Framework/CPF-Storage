package org.commonprovenance.framework.store.persistence.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.entity.TokenEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenNeo4jRepositoryClient extends ReactiveNeo4jRepository<TokenEntity, String> {
}
