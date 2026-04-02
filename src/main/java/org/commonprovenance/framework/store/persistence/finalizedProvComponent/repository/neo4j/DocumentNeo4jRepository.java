package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.DocumentNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class DocumentNeo4jRepository implements DocumentRepository {
  private final DocumentNeo4jRepositoryClient client;

  public DocumentNeo4jRepository(
      DocumentNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<DocumentNode> save(DocumentNode document) {
    return client.save(document);
  }

  @Override
  public Flux<DocumentNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<DocumentNode> findByIdentifier(String identifier) {
    return client.getIdByIdentifier(identifier)
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Document with identifier '" + identifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one document with identifier '" + identifier + "'!"));
  }
  }

}
