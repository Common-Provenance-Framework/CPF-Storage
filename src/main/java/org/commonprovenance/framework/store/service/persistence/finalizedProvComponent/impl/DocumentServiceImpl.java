package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.utils.DocumentUtils;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import cz.muni.fi.cpm.model.CpmDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentServiceImpl implements DocumentService {
  private final DocumentPersistence persistence;
  private final StoreWebService storeWebService;

  public DocumentServiceImpl(
      DocumentPersistence persistence,
      StoreWebService storeWebService) {
    this.persistence = persistence;
    this.storeWebService = storeWebService;
  }

  private String buildHeader(String methodName, String message) {
    return "[DocumentService].[" + methodName + "].(" + message + ")";
  }

  private String getIdentifier(Document document) {
    return document.getCpmDocument()
        .map(CpmDocument::getBundleId)
        .map(QualifiedName::toString)
        .orElse("unknown");
  }

  @Override
  public Mono<Document> storeDocument(Document document) {
    return this.persistence.create(document);
  }

  @Override
  public Flux<Document> getAllDocuments() {
    return this.persistence.getAll();
  }

  @Override
  public Mono<Document> getDocumentByIdentifier(String identifier) {
    return this.persistence.getByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return this.persistence.existsByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> exists(Document document) {
    return MONO.makeSureNotNull(document)
        .map(Document::getIdentifier)
        .flatMap(MONO::fromOptional)
        .flatMap(this::existsByIdentifier)
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> notExists(Document document) {
    return this.exists(document)
        .map(exists -> !exists);
  }

  @Override
  public Mono<Void> checkDocumentDoesNotExists(Document document) {
    return Mono.just(document)
        .flatMap(MONO.makeSureAsync(
            this::notExists,
            ConflictException::new,
            doc -> "Document with identifier '" + doc.getIdentifier().orElse("unknown") + "' exists!!"))
        .then();
  }

  @Override
  public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
    return this.persistence.getOrganizationIdentifierByIdentifier(identifier);
  }

  @Override
  public Mono<Void> checkSpecForwardConnectorsResolvable(Document document) {
    return MONO.makeSureNotNull(document)
        .flatMap(MONO.liftEffectToMono(DocumentUtils::getCpmDocument))
        .flatMapMany(MONO.<CpmDocument, Entity> liftEffectToFlux(DocumentUtils::getSpecForwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.header(buildHeader("checkSpecForwardConnectorsResolvable", "DocumentId: " + getIdentifier(document))));
  }

  @Override
  public Mono<Void> checkBackwardConnectorsResolvable(Document document) {
    return MONO.makeSureNotNull(document)
        .flatMap(MONO.liftEffectToMono(DocumentUtils::getCpmDocument))
        .flatMapMany(MONO.<CpmDocument, Entity> liftEffectToFlux(DocumentUtils::getBackwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.header(buildHeader("checkBackwardConnectorsResolvable", "DocumentId: " + getIdentifier(document))));
  }

}
