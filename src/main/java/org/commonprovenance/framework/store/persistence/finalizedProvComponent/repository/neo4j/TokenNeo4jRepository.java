package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.TokenNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TokenNeo4jRepository implements TokenRepository {
  private final TokenNeo4jRepositoryClient client;

  public TokenNeo4jRepository(
      TokenNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<TokenNode> save(TokenNode token) {
    return client.save(token);
  }

  @Override
  public Flux<TokenNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TokenNode> getTokenByDocumentIdentifier(String documentIdentifier) {
    return client.findTokenIdsByDocumentIdentifier(documentIdentifier)
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "Token with document identifier '" + documentIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "There is more then one Token with document identifier '" + documentIdentifier + "'!"));
  }

  @Override
  public Mono<String> getOrganizationIdentifierByDocumentIdentifier(String documentIdentifier) {
    return client.findOrganizationIdentifierByDocumentIdentifier(documentIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "Token with document identifier '" + documentIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "There is more then one Token with document identifier '" + documentIdentifier + "'!"));
  }

}
