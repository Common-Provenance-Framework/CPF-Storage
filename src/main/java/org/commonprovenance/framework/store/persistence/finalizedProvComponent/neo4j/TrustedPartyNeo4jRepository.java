package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TrustedPartyNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.TrustedPartyNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class TrustedPartyNeo4jRepository implements TrustedPartyRepository {
  private final String LOG_PREFIX = "TrustedPartyNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TrustedPartyNeo4jRepository.class);

  private final TrustedPartyNeo4jRepositoryClient trustedPartyClient;

  public TrustedPartyNeo4jRepository(
      TrustedPartyNeo4jRepositoryClient trustedPartyClient) {
    this.trustedPartyClient = trustedPartyClient;
  }

  @Override
  public Mono<Void> create(TrustedParty trustedParty) {
    return Mono.just(trustedParty)
        .map(TrustedPartyNodeFactory::build)
        .flatMap(trustedPartyClient::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party has been saved into DB."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Trusted Party has not been saved into DB!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Trusted Party has not been saved into DB!")));
  }

  @Override
  public Mono<TrustedParty> findByName(String name) {
    return trustedPartyClient.findIdByName(name)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("TrustedParty with name '" + name + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one TrustedParty with name '" + name + "'!"))
        .flatMap(trustedPartyClient::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("TrustedParty with name '" + name + "' has not found!")))
        .map(TrustedPartyFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party with name '" + name + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Trusted Party with name '" + name + "' failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Search Trusted Party with name '" + name + "' has been failed!")));
  }

  @Override
  public Mono<TrustedParty> findDefault() {
    return trustedPartyClient.findDefaultId()
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Default TrustedParty has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one default TrustedParty!"))
        .flatMap(trustedPartyClient::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Default TrustedParty has not been found!")))
        .map(TrustedPartyFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Default TrustedParty has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search default Trusted Party has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Search default Trusted Party has been failed!")));
  }

  @Override
  public Mono<TrustedParty> findByOrganizationIdentifier(String organizationIdentifier) {
    return trustedPartyClient.findByOrganizationIdentifier(organizationIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Trusted Party for organization with identifier '" + organizationIdentifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("Organization with identifier '" + organizationIdentifier + "' has more then one TrustedParty!"))
        .map(TrustedPartyFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party for organization with identifier '" + organizationIdentifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Trusted Party for organization with identifier '" + organizationIdentifier + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Search Trusted Party for organization with identifier '" + organizationIdentifier + "' has been failed!")));
  }

  @Override
  public Mono<String> findUrlByOrganizationIdentifier(String organizationIdentifier) {
    return trustedPartyClient.findIdByOrganizationIdentifier(organizationIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Organization with identifier '" + organizationIdentifier + "' has no TrustedParty!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("Organization with identifier '" + organizationIdentifier + "' has more then one TrustedParty!"))
        .flatMap(trustedPartyClient::findUrlById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Trusted Party URL for organization with identifier '" + organizationIdentifier + "' has not been found!")))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party URL for organization with identifier '" + organizationIdentifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Trusted Party URL for organization with identifier '" + organizationIdentifier + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Search Trusted Party URL for organization with identifier '" + organizationIdentifier + "' has been failed!")));
  }

}
