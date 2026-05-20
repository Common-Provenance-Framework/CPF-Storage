package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.TokenNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.neo4j.MetaBundleNeo4jRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TokenNeo4jRepository implements TokenRepository {
  private final String LOG_PREFIX = "TrustedPartyNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaBundleNeo4jRepository.class);

  private final TokenNeo4jRepositoryClient client;

  public TokenNeo4jRepository(
      TokenNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<Void> save(Token token) {
    return Mono.just(token)
        .flatMap(NodeFactory::toEntity)
        .flatMap(client::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "New Token has been created."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "New Token has not been created!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("New Token has not been created!")));
  }

  @Override
  public Mono<Token> getTokenByDocumentIdentifier(String documentIdentifier) {
    return client.findTokenIdsByDocumentIdentifier(documentIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Token id for document with identifier '" + documentIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one Token ids for document with identifier '" + documentIdentifier + "'!"))
        .flatMap(client::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Token for document with identifier '" + documentIdentifier + "' has not been found!")))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token for document with identifier '" + documentIdentifier + "' has been found."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Search Token for document with identifier '" + documentIdentifier + "' has been failed!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Search Token for document with identifier '" + documentIdentifier + "' has been failed!")));

  }

}
