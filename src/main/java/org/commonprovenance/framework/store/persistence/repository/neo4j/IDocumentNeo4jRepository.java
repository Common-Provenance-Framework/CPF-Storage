package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDocumentNeo4jRepository extends ReactiveNeo4jRepository<DocumentEntity, String> {
}
