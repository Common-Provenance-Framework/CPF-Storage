package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.OrganizationRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Validated
public class OrganizationPersistenceImpl implements OrganizationPersistence {

  private final OrganizationRepository repository;

  public OrganizationPersistenceImpl(
      OrganizationRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<Organization> create(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while creating new Organization"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> update(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while updating existing Organization"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Flux<Organization> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading documents"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Organization> getByIdentifier(@NotNull String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Organization identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"))
        .switchIfEmpty(Mono.error(new NotFoundException("Organization with id '" + identifier + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Void> deleteByIdentifier(@NotNull String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Organization identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::deleteByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"));
  }

}
