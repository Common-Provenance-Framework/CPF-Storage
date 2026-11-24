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
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
        ? this.client.sendHeadRequest(this.client.store(), this.buildUriPartFromResourcePath(resourcePath), Map.of())
        : this.client.sendHeadRequest(this.client.custom(resourcePath), "", Map.of()))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Ping Resource at path '" + resourcePath + "' has been passed."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Ping Resource at path '"
              + resourcePath
              + "' has been failed!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> new NotFoundException("Resource not found at: " + notFound.getRequest().getURI()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Ping Resource at path '" + resourcePath + "' has been failed!")));
  }

  @Override
  public Mono<Token> getTokenByResourcePath(String resourcePath) {
    return (resourcePath.startsWith(this.client.getStoreUrl())
        ? this.client.sendGetOneRequest(
            this.client.store(),
            this.buildUriPartFromResourcePath(resourcePath, "token"),
            TokenResponseDTO.class,
            Map.of())
        : this.client.sendGetOneRequest(
            this.client.custom(resourcePath),
            "/token",
            TokenResponseDTO.class,
            Map.of()))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token for document at path '" + resourcePath + "' has been fetched."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Token for document at path '"
              + resourcePath
              + "' has not been fetched!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> new NotFoundException("Resource not found at: " + notFound.getRequest().getURI()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Token for document at path '" + resourcePath + "' has not been fetched!")));
  }

  private String buildUriPartFromResourcePath(String resourcePath) {
    return buildUriPartFromResourcePath(resourcePath, "");
  }

  private String buildUriPartFromResourcePath(String resourcePath, String suffix) {
    return resourcePath.substring(this.client.getStoreUrl().length()) + (suffix.isBlank() ? "" : "/" + suffix);
  }

}
