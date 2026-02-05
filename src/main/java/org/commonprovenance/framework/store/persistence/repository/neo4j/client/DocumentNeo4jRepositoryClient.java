package org.commonprovenance.framework.store.persistence.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentNeo4jRepositoryClient extends ReactiveNeo4jRepository<DocumentEntity, String> {
}
