package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TokenNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.TokenNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TokenNeo4jRepository implements TokenRepository {
  private final String LOG_PREFIX = "TokenNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenNeo4jRepository.class);

  private final TokenNeo4jRepositoryClient client;

  public TokenNeo4jRepository(TokenNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<Void> save(Token token) {
    return Mono.just(token)
        .map(TokenNodeFactory::build)
        .flatMap(client::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been saved into DB."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Token has not been saved into DB!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Token has not been saved into DB!")));
  }

  @Override
  public Mono<Token> getTokenByDocumentIdentifier(String documentIdentifier) {
    return Mono.just(documentIdentifier)
        .flatMapMany(client::findTokenIdsByDocumentIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Token id for document with identifier '" + documentIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one Token ids for document with identifier '" + documentIdentifier + "'!"))
        .flatMap(client::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Token for document with identifier '" + documentIdentifier + "' has not been found!")))
        .flatMap(MONO.liftEffectToMono(TokenFactory::buildWithRelations))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token for document with identifier '" + documentIdentifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Token for document with identifier '" + documentIdentifier + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Search Token for document with identifier '" + documentIdentifier + "' has been failed!")));

  }

  @Override
  public Mono<String> getTokenIdByDocumentIdentifier(String documentIdentifier) {
    return Mono.just(documentIdentifier)
        .flatMapMany(client::findTokenIdsByDocumentIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Token id for document with identifier '" + documentIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one Token ids for document with identifier '" + documentIdentifier + "'!"))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token ID for document with identifier '" + documentIdentifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Token ID for document with identifier '" + documentIdentifier + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Search Token ID for document with identifier '" + documentIdentifier + "' has been failed!")));
  }

  @Override
  public Function<Document, Mono<Void>> connectWasIssuedBy(Optional<TrustedParty> maybeTrustedParty) {
    return (Document document) -> MONO.combineM(Mono.justOrEmpty(document)
        .flatMap(MONO.liftOptionalToMono(
            Document::getIdentifier,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .flatMap(this::getTokenIdByDocumentIdentifier),
        Mono.justOrEmpty(maybeTrustedParty)
            .map(TrustedParty::getName),
        (tokenId, trustedPartyName) -> this.client.createWasIssuedByRelationship(tokenId, trustedPartyName))
        .then();
  }

}
