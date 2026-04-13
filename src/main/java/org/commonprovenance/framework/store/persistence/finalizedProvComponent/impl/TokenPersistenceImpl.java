package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TokenPersistenceImpl implements TokenPersistence {

  private final TokenRepository repository;

  public TokenPersistenceImpl(
      TokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Token> create(Token token) {
    return MONO.<Token>makeSureNotNullWithMessage("Token can not be 'null'!").apply(token)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TokenPersistence - Error while creating new Token"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Flux<Token> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("TokenPersistence - Error while reading tokens"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Token> getByDocumentIdentifier(String documentIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(documentIdentifier)
        .flatMap(repository::getTokenByDocumentIdentifier)
        .onErrorResume(MONO.exceptionWrapper("TokenPersistence - Error while reading Token"))
        .flatMap(ModelFactory::toDomain)
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException(
                "Token with document identifier '" + documentIdentifier + "' has not been found!"))));
  }

}