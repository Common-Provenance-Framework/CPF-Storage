package org.commonprovenance.framework.store.web.trustedParty.client.webFlux;

import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class TrustedPartyWebFluxClient implements TrustedPartyClient {
  private final WebClient client;

  public TrustedPartyWebFluxClient(WebClient client) {
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
