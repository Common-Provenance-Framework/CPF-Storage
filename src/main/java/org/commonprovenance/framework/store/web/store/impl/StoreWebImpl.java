package org.commonprovenance.framework.store.web.store.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.store.StoreWeb;
import org.commonprovenance.framework.store.web.store.dto.response.TokenResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import reactor.core.publisher.Mono;

@Component
public class StoreWebImpl implements StoreWeb {
  private final String LOG_PREFIX = "StoreWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(StoreWebImpl.class);

  private final Client client;

  public StoreWebImpl(Client client) {
    this.client = client;
  }

  @Override
  public Mono<Void> pingByResourcePath(String resourcePath) {

    return (resourcePath.startsWith(this.client.getStoreUrl())
        ? this.client.sendHeadRequest(this.client.store(), resourcePath.substring(this.client.getStoreUrl().length()), Map.of())
        : this.client.sendHeadRequest(this.client.custom(resourcePath), "", Map.of()))
        .onErrorResume(NotFound.class, notFound -> {
          return Mono.error(
              () -> new NotFoundException("Resource not found at: " + notFound.getRequest().getURI()));
        })
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
    return (resourcePath.startsWith(this.client.getStoreUrl())
        ? this.client.sendGetOneRequest(
            this.client.store(),
            resourcePath.substring(this.client.getStoreUrl().length()) + "/token",
            TokenResponseDTO.class,
            Map.of())
        : this.client.sendGetOneRequest(
            this.client.custom(resourcePath),
            "/token",
            TokenResponseDTO.class,
            Map.of()))
        .onErrorResume(NotFound.class, notFound -> {
          return Mono.error(
              () -> new NotFoundException("Resource not found at: " + notFound.getRequest().getURI()));
        })
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
