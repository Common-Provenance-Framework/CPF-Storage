package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.DocumentNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.DocumentNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class DocumentNeo4jRepository implements DocumentRepository {
  private final String LOG_PREFIX = "DocumentNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentNeo4jRepository.class);

  private final DocumentNeo4jRepositoryClient client;

  public DocumentNeo4jRepository(
      DocumentNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<Void> save(Document document) {
    return Mono.just(document)
        .map(DocumentNodeFactory::buildWithRelations)
        .flatMap(client::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Document has been saved into DB."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Document has not been saved into DB!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Document has not been saved into DB!")));
  }

  @Override
  public Mono<Document> findByIdentifier(String identifier) {
    return Mono.just(identifier)
        .flatMapMany(client::getIdByIdentifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Document with identifier '" + identifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one document with identifier '" + identifier + "'!"))
        .flatMap(client::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Document with identifier '" + identifier + "' has not been found!")))
        .flatMap(MONO.liftEffectToMono(DocumentFactory::buildWithRelations))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Document with identifier '" + identifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Search Document with identifier '" + identifier + "' has been failed!\n" + throwable.getMessage());

        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Search Document with identifier '" + identifier + "' has been failed!")));
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return Mono.just(identifier)
        .flatMap(client::countByIdentifier)
        .flatMap(MONO.<Integer> makeSure(
            occurrence -> occurrence == 0 || occurrence == 1,
            occurrence -> new ConflictException("There is more then one document with identifier '" + identifier + "'!")))
        .map(occurrence -> occurrence == 1)
        .doOnSuccess(exists -> {
          if (exists)
            LOGGER.trace(LOG_PREFIX + "Document with identifier '" + identifier + "' exists.");
          else
            LOGGER.trace(LOG_PREFIX + "Document with identifier '" + identifier + "' not exists.");
        })
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Document existence validation with identifier '" + identifier + "' has been failed!\n" + throwable.getMessage()))
        .onErrorMap(
            ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Document existence validation with identifier '" + identifier + "' has been failed!")));
  }

  @Override
  public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
    return client.findOrganizationIdentifierByIdentifier(identifier)
        .single()
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "Document with identifier '" + identifier + "' has not been found, or is not associated with any Organization!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "There is more then one document with identifier '" + identifier + "'!"))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Organization identifier for Document with identifier '" + identifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Organization identifier for Document with identifier '" + identifier + "' has not been found!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Organization identifier for Document with identifier '" + identifier + "' has not been found!")));

  }

}
