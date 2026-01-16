package org.commonprovenance.framework.storage.persistence.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import org.commonprovenance.framework.storage.persistence.neo4j.entities.DocumentEntity;

@Repository
public interface IDocumentNeo4jRepository extends ReactiveNeo4jRepository<DocumentEntity, String> {
}
