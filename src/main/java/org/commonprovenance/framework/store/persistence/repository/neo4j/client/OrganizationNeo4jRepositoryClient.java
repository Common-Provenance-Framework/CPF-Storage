package org.commonprovenance.framework.store.persistence.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface OrganizationNeo4jRepositoryClient extends ReactiveNeo4jRepository<OrganizationEntity, String> {

  @Query("MATCH (o:Organization {name: \"myorg_6\"}) RETURN o.name as name, o.clientCertificate as clientCertificate, o.intermediateCertificates as intermediateCertificates, o.id as id")
  Mono<OrganizationEntity> findByName(String name);
}
