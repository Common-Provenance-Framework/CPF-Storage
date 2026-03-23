package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Validated
public class TokenPersistenceImpl implements TokenPersistence {

  private final TokenRepository repository;

  public TokenPersistenceImpl(
      TokenRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<Token> create(@NotNull Token token) {
    return MONO.<Token>makeSureNotNullWithMessage("Token can not be 'null'!").apply(token)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TokenNeo4jRepository - Error while creating new Document"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Flux<Token> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("TokenNeo4jRepository - Error while reading documents"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Token> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Token Id can not be 'null'!").apply(id)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("TokenNeo4jRepository - Error while reading document"))
        .flatMap(ModelFactory::toDomain)
        .switchIfEmpty(
            Mono.error(new NotFoundException("Token with id '" + id.toString() + "' has not been found!")));
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Token Id can not be 'null'!").apply(id)
        .flatMap(repository::deleteById)
        .onErrorResume(MONO.exceptionWrapper("TokenNeo4jRepository - Error while reading document"));
  }
}
