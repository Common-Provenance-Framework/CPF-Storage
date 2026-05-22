package org.commonprovenance.framework.store.web.trustedParty.client.reactive;

import java.util.Map;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class ClientTrustedPartyReactive implements ClientTrustedParty {
  private final WebClient client;
  private final String defaultUrl;

  public ClientTrustedPartyReactive(WebConfig config) {
    this.client = config.getDefaultTrustedPartyWebClient();
    this.defaultUrl = config.getTrustedPartyUrl();
  }

  @Override
  public String getDefaultTrustedPartyUrl() {
    return this.defaultUrl;
  }

  @Override
  public WebClient buildWebClient(String trustedPartyUrl) {
    return WebClient.builder()
        .baseUrl(trustedPartyUrl)
        .defaultHeader("Accept", "application/json")
        .build();
  }

  public <T> Function<WebClient, Mono<T>> sendCustomGetOneRequest(
      String uri,
      Class<T> responseType,
      Map<String, String> queryParams) {
    return (WebClient customClient) -> customClient
        .get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(responseType);
  }

  public <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(responseType);
  }

  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType, Map<String, String> queryParams) {
    return this.client.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToFlux(responseType);
  }

  public <T> Function<WebClient, Flux<T>> sendCustomGetManyRequest(
      String uri,
      Class<T> responseType,
      Map<String, String> queryParams) {
    return (WebClient customClient) -> customClient.get()
        .uri(buildUriWithParams(uri, queryParams))
        .retrieve()
        .bodyToFlux(responseType);
  }

  public <T, B> Function<B, Mono<T>> sendPostRequest(String uri, Class<T> responseType) {
    return (B body) -> this.client.post()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T, B> Function<WebClient, Function<B, Mono<T>>> sendCustomPostRequest(String uri, Class<T> responseType) {
    return (WebClient customClient) -> (B body) -> customClient
        .post()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T, B> Function<B, Mono<T>> sendPutRequest(String uri, Class<T> responseType) {
    return (B body) -> this.client.put()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T, B> Function<WebClient, Function<B, Mono<T>>> sendCustomPutRequest(String uri, Class<T> responseType) {
    return (WebClient customClient) -> (B body) -> customClient
        .put()
        .uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType) {
    return this.client.delete()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T> Function<WebClient, Mono<T>> sendCustomDeleteRequest(String uri, Class<T> responseType) {
    return (WebClient customClient) -> customClient.delete()
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

}
