package org.commonprovenance.framework.store.web.store.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.web.store.StoreWeb;
import org.commonprovenance.framework.store.web.store.client.ClientStore;
import org.commonprovenance.framework.store.web.store.dto.response.TokenResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class StoreWebImpl implements StoreWeb {
  private final String LOG_PREFIX = "StoreWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(StoreWebImpl.class);

  private final ClientStore client;

  public StoreWebImpl(ClientStore client) {
    this.client = client;
  }

  @Override
  public Mono<Void> pingByResourcePath(String resourcePath) {
    return Mono.just(resourcePath)
        .flatMap(this.client::sendHeadRequest)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Ping Resource at path '" + resourcePath + "' has been passed."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Ping Resource at path '" + resourcePath + "' has been failed!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Ping Resource at path '" + resourcePath + "' has been failed!")));
  }

  @Override
  public Mono<Token> getTokenByResourcePath(String resourcePath) {
    return Mono.just(resourcePath)
        .map(path -> path + "/token")
        .flatMap(this.client.sendGetOneRequest(TokenResponseDTO.class))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token for document at path '" + resourcePath + "' has been fetched."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Token for document at path '" + resourcePath + "' has not been fetched!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Token for document at path '" + resourcePath + "' has not been fetched!")));
  }

}
