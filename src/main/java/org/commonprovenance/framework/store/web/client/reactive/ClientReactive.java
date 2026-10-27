package org.commonprovenance.framework.store.web.client.reactive;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ClientReactive implements Client {
  private final String LOG_PREFIX = "Client: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientReactive.class);

  private final WebClient base;
  private final WebClient storeClient;
  private final WebClient trustedPartyClient;
  private final String defaultTrustedPartyUrl;
  private final String storeUrl;

  public ClientReactive(
      WebClient base,
      @Qualifier(WebConfig.STORE_CLIENT) WebClient storeClient,
      @Qualifier(WebConfig.TRUSTED_PARTY_CLIENT) WebClient trustedPartyClient,
      WebConfig config) {
    this.base = base;
    this.storeClient = storeClient;
    this.trustedPartyClient = trustedPartyClient;
    this.defaultTrustedPartyUrl = config.getTrustedPartyBaseUrl();
    this.storeUrl = config.getStoreBaseUrl();
  }

  @Override
  public WebClient store() {
    return this.storeClient;
  }

  @Override
  public WebClient trustedParty() {
    return this.trustedPartyClient;
  }

  @Override
  public WebClient trustedParty(Optional<String> customBaseUrl) {
    return customBaseUrl.map(this::custom).orElse(this.trustedPartyClient);
  }

  @Override
  public WebClient custom(String baseUrl) {
    return this.base.mutate().baseUrl(baseUrl).build();
  }

  @Override
  public String getDefaultTrustedPartyUrl() {
    return this.defaultTrustedPartyUrl;
  }

  @Override
  public String getStoreUrl() {
    return this.storeUrl;
  }

  @Override
  public Mono<Void> sendHeadRequest(WebClient client, String uri, Map<String, String> queryParams) {
    return this.log(client.head()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToMono(Void.class));
  }

  @Override
  public <T> Mono<T> sendGetOneRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.log(client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToMono(responseType));
  }

  @Override
  public <T> Flux<T> sendGetManyRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.log(client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToFlux(responseType));
  }

  @Override
  public <T, B> Function<B, Mono<T>> sendPostRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams) {
    return (B body) -> this.log(client.post()
        .uri(buildUriWithParams(uri, queryParams))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType));
  }

  @Override
  public <T, B> Function<B, Mono<T>> sendPutRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams) {
    return (B body) -> this.log(client.put()
        .uri(buildUriWithParams(uri, queryParams))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType));
  }

  @Override
  public <T> Mono<T> sendDeleteRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.log(client.delete()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToMono(responseType));
  }

  private <T> Mono<T> log(Mono<T> mono) {
    return mono
        .doOnNext(data -> LOGGER.trace(LOG_PREFIX + data))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + throwable.getMessage()))
        .doOnError(
            WebClientResponseException.class,
            response -> LOGGER.debug(LOG_PREFIX + response.getResponseBodyAsString()));
  }

  private <T> Flux<T> log(Flux<T> flux) {
    return flux
        .doOnNext(data -> LOGGER.trace(LOG_PREFIX + data))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + throwable.getMessage()))
        .doOnError(
            WebClientResponseException.class,
            response -> LOGGER.debug(LOG_PREFIX + response.getResponseBodyAsString()));
  }

  private Function<UriBuilder, java.net.URI> buildUriWithParams(String uri, Map<String, String> queryParams) {
    return (UriBuilder uriBuilder) -> {
      UriBuilder builder = uriBuilder.path(uri);
      Optional.ofNullable(queryParams)
          .ifPresent(params -> params.forEach(builder::queryParam));
      return builder.build();
    };
  }
}
