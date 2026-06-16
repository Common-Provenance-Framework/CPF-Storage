package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.OrganizationNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.OrganizationNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class OrganizationNeo4jRepository implements OrganizationRepository {
  private final String LOG_PREFIX = "OrganizationNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationNeo4jRepository.class);

  private final OrganizationNeo4jRepositoryClient client;

  public OrganizationNeo4jRepository(OrganizationNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<Void> save(Organization organization) {
    return Mono.just(organization)
        .map(OrganizationNodeFactory::build)
        .flatMap(client::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Organization has been saved into DB."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Organization has not been saved into DB!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Organization has not been saved into DB!")));
  }

  @Override
  public Mono<Void> connectTrusts(Organization organization) {
    return MONO.combineM(
        Mono.just(organization)
            .map(Organization::getIdentifier),
        Mono.just(organization)
            .flatMap(MONO.liftOptionalToMono(Organization::getTrustedParty))
            .map(TrustedParty::getName),
        client::createTrustsRelationship)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(
            LOG_PREFIX + "Organization with identifier '" + organization.getIdentifier() + "' has been connected to TrustedParty with name '"
                + organization.getTrustedParty().map(TrustedParty::getName).orElse("unknown") + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Organization with identifier '" + organization.getIdentifier() + "' has not been connected to TrustedParty with name'"
                + organization.getTrustedParty().map(TrustedParty::getName).orElse("unknown") + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Organization has not been connected to TrustedParty!")));
  }

  @Override
  public Mono<Organization> findByIdentifier(String identifier) {
    return Mono.just(identifier)
        .flatMapMany(client::getByIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Organization with identifier '" + identifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one organization with identifier '" + identifier + "'!"))
        .map(OrganizationFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Organization with identifier '" + identifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Organization with identifier '" + identifier + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Search Organization with identifier '" + identifier + "' has been failed!")));

  }

  // TODO: this should be moved into Document
  @Override
  public Function<Document, Mono<Void>> connectOwns(String identifier) {
    return (Document document) -> MONO.combineM(
        MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be 'null'!")
            .apply(identifier),
        Mono.justOrEmpty(document.getIdentifier())
            .flatMap(MONO.<String> makeSureNotNullWithMessage("Document identifier can not be 'null'!")),
        client::createOwnsRelationship)
        .then();
    // ...
  }

}
