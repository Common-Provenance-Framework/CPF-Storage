package org.commonprovenance.framework.store.web.trustedParty.client.webFlux;

import java.util.function.Function;

import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.client.webFlux.config.WebConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class ClientWebFlux implements Client {
  private final WebClient client;
  private final WebConfig config;

  public ClientWebFlux(WebClient client, WebConfig config) {
    this.client = client;
    this.config = config;
  }

  @Override
  public String getUrl() {
    return this.config.getTrustedPartyUrl();
  }

  public <T> Function<WebClient, Mono<T>> sendCustomGetOneRequest(String uri, Class<T> responseType) {
    return (WebClient customClient) -> customClient
        .get()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType) {
    return this.client.get()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }

  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType) {
    return this.client.get()
        .uri(uri)
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

  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType) {
    return this.client.post()
        .uri(uri)
        .retrieve()
        .bodyToMono(responseType);
  }
}
