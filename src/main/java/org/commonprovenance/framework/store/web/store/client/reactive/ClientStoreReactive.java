package org.commonprovenance.framework.store.web.store.client.reactive;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.store.client.ClientStore;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class ClientStoreReactive implements ClientStore {
  private final WebClient client;

  public ClientStoreReactive(WebConfig config) {
    this.client = config.getStoreWebClient();
  }

  @Override
  public Mono<Void> sendHeadRequest(String uri) {
    return this.client.head()
        .uri(uri)
        .retrieve()
        .onStatus(
            status -> status.value() == 404,
            response -> Mono.error(() -> new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(Void.class);
  }

  @Override
  public <T> Function<String, Mono<T>> sendGetOneRequest(Class<T> responseType) {
    return (String uri) -> this.client.get()
        .uri(uri)
        .retrieve()
        .onStatus(
            status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + response.request().getURI())))
        .bodyToMono(responseType);
  }
}
