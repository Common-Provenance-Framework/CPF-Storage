package org.commonprovenance.framework.store.web.trustedParty.client.reactive;

import java.util.Map;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.impl.TokenWebImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class ClientTrustedPartyReactive implements ClientTrustedParty {
  private final String LOG_PREFIX = "ClientTrustedParty: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientTrustedParty.class);

  private final WebClient client;
  private final String defaultBaseUrl;

  public ClientTrustedPartyReactive(WebConfig config) {
    this.client = config.getDefaultTrustedPartyWebClient();
    this.defaultBaseUrl = config.getTrustedPartyBaseUrl();
  }

  @Override
  public String getDefaultTrustedPartyUrl() {
    return this.defaultBaseUrl;
  }

  @Override
  public <T> Function<String, Mono<T>> sendCustomGetOneRequest(
      String uri,
      Class<T> responseType,
      Map<String, String> queryParams) {
    return (String trustedPartyBaseUrl) -> this.buildWebClient(trustedPartyBaseUrl)
        .get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(responseType);
  }

  @Override
  public <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(responseType);
  }

  @Override
  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToFlux(responseType);
  }

  @Override
  public <T> Function<String, Flux<T>> sendCustomGetManyRequest(
      String uri,
      Class<T> responseType,
      Map<String, String> queryParams) {
    return (String trustedPartyBaseUrl) -> this.buildWebClient(trustedPartyBaseUrl)
        .get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToFlux(responseType);
  }

  @Override
  public <T, B> Function<B, Mono<T>> sendPostRequest(String uri, Class<T> responseType) {
    return (B body) -> this.client.post()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        // TODO: Test and finish
        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class).defaultIfEmpty("")
            .flatMap(errorBody -> {
              LOGGER.warn(LOG_PREFIX + "HTTP " + response.statusCode().value() + " body: " + errorBody);
              return Mono.error(new RuntimeException(
                  "Request failed: " + response.statusCode().value() + ", body=" + errorBody));
            }))
        .bodyToMono(responseType)
        .doOnError(WebClientResponseException.class, ex -> System.err.println("Response body from exception: " + ex.getResponseBodyAsString()));
  }

  @Override
  public <T, B> Function<String, Function<B, Mono<T>>> sendCustomPostRequest(String uri, Class<T> responseType) {
    return (String trustedPartyBaseUrl) -> (B body) -> this.buildWebClient(trustedPartyBaseUrl)
        .post()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        // TODO: Test and finish
        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class).defaultIfEmpty("")
            .flatMap(errorBody -> {
              LOGGER.warn(LOG_PREFIX + "HTTP " + response.statusCode().value() + " body: " + errorBody);
              return Mono.error(new RuntimeException(
                  "Request failed: " + response.statusCode().value() + ", body=" + errorBody));
            }))
        .bodyToMono(responseType)
        .doOnError(WebClientResponseException.class, ex -> System.err.println("Response body from exception: " + ex.getResponseBodyAsString()));
  }

  @Override
  public <T, B> Function<B, Mono<T>> sendPutRequest(String uri, Class<T> responseType) {
    return (B body) -> this.client.put()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  @Override
  public <T, B> Function<String, Function<B, Mono<T>>> sendCustomPutRequest(String uri, Class<T> responseType) {
    return (String trustedPartyBaseUrl) -> (B body) -> this.buildWebClient(trustedPartyBaseUrl)
        .put()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  @Override
  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType) {
    return this.client.delete()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }

  @Override
  public <T> Function<String, Mono<T>> sendCustomDeleteRequest(String uri, Class<T> responseType) {
    return (String trustedPartyBaseUrl) -> this.buildWebClient(trustedPartyBaseUrl)
        .delete()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }

  private Function<UriBuilder, java.net.URI> buildUriWithParams(String uri, Map<String, String> queryParams) {
    return (UriBuilder uriBuilder) -> {
      UriBuilder builder = uriBuilder.path(uri);
      if (queryParams != null) {
        queryParams.forEach(builder::queryParam);
      }
      return builder.build();
    };
  }

  private WebClient buildWebClient(String trustedPartyBaseUrl) {
    return WebClient.builder()
        .baseUrl(trustedPartyBaseUrl)
        .defaultHeader("Accept", "application/json")
        .build();
  }
}
