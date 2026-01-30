package org.commonprovenance.framework.store.web.trustedParty.webFlux.cient;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebFluxClient {
  private final WebClient client;

  public WebFluxClient(WebClient client) {
    this.client = client;
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

  public <T, B> Mono<T> sendPostRequest(String uri, B body, Class<T> responseType) {
    return this.client.post()
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
