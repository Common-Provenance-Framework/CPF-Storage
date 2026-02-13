package org.commonprovenance.framework.store.persistence.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.OrganizationPersistence;
import org.commonprovenance.framework.store.persistence.entity.factory.EntityFactory;
import org.commonprovenance.framework.store.persistence.repository.OrganizationRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
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
  @NotNull
  public Mono<Organization> create(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(org -> Mono.zip(
            EntityFactory.toEntity(org),
            EntityFactory.toEntity(org.getTrustedParty())))
        .map(tuple -> tuple.getT1().withTrustedParty(tuple.getT2()))
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while creating new Organization"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> update(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be 'null'!").apply(organization)
        .flatMap(EntityFactory::toEntity)
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
  public Mono<Organization> getById(@NotNull UUID id) {
    return MONO.<UUID>makeSureNotNullWithMessage("Organization Id can not be 'null'!").apply(id)
        .map(UUID::toString)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"))
        .switchIfEmpty(Mono.error(new NotFoundException("Organization with id '" + id + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> getByName(@NotNull String name) {
    return MONO.<String>makeSureNotNullWithMessage("Organization name can not be 'null'!").apply(name)
        .flatMap(repository::findByName)
        .onErrorResume(
            MONO.exceptionWrapper(e -> "OrganizationPersistence - Error while reading organization by name: " + name
                + ". Message: " + e.getMessage() + "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))
        .switchIfEmpty(Mono.error(new NotFoundException("Organization with name '" + name + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("Organization Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::deleteById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"));
  }

}
