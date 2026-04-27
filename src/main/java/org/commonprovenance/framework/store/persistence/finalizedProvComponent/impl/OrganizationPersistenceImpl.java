package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.OrganizationRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationPersistenceImpl implements OrganizationPersistence {

  private final OrganizationRepository repository;

  public OrganizationPersistenceImpl(
      OrganizationRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Organization> create(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while creating new Organization"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Organization> update(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while updating existing Organization"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Flux<Organization> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while reading Organizations"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Organization> getByIdentifier(String identifier) {
    return MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while reading Organization"))
        .switchIfEmpty(
            Mono.error(new NotFoundException("Organization with identifier '" + identifier + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Boolean> connectDocument(Document document) {
    return MONO.<Document> makeSureNotNullWithMessage("Document can not be 'null'!")
        .apply(document)
        .flatMap(doc -> MONO.combineM(
            MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be 'null'!")
                .apply(doc.getOrganizationIdentifier()),
            Mono.justOrEmpty(doc.getIdentifier())
                .flatMap(MONO.<String> makeSureNotNullWithMessage("Document identifier can not be 'null'!")),
            repository::connectOwns))
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while connecting document ownership"));
  }

}
