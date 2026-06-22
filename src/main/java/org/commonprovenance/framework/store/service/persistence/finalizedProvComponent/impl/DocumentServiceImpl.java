package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.springframework.stereotype.Service;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@Service
public class DocumentServiceImpl implements DocumentService {
  private final DocumentRepository repository;
  private final StoreWebService storeWebService;

  public DocumentServiceImpl(
      DocumentRepository repository,
      StoreWebService storeWebService) {
    this.repository = repository;
    this.storeWebService = storeWebService;
  }

  private String getIdentifier(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(Document::getIdentifier)
        .getOrElse("unknown");
  }

  @Override
  public Mono<Void> storeDocument(Document document) {
    return this.repository.save(document);
  }

  @Override
  public Mono<Document> getDocumentByIdentifier(String identifier) {
    return this.repository.findByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return this.repository.existsByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> exists(Document document) {
    return MONO.makeSureNotNull(document)
        .flatMap(MONO.liftEffectToMono(Document::getIdentifier))
        .flatMap(this::existsByIdentifier)
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> notExists(Document document) {
    return this.exists(document)
        .map(exists -> !exists);
  }

  @Override
  public Mono<Void> checkDocumentDoesNotExists(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .flatMap(MONO.makeSureAsync(
            this::notExists,
            ConflictException::new,
            doc -> "Document with identifier '" + getIdentifier(doc) + "' exists!!"))
        .then();
  }

  @Override
  public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
    return this.repository.getOrganizationIdentifierByIdentifier(identifier);
  }

  @Override
  public Mono<Void> checkSpecForwardConnectorsResolvable(Document document) {
    return Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getSpecForwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("checkSpecForwardConnectorsResolvable '" + getIdentifier(document) + "'' failed!")));

  }

  @Override
  public Mono<Void> checkBackwardConnectorsResolvable(Document document) {
    return Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getBackwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("checkBackwardConnectorsResolvable '" + getIdentifier(document) + "'' failed!")));
  }

}
