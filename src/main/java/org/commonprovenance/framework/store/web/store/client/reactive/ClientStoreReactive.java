package org.commonprovenance.framework.store.web.store.client.reactive;

import org.springframework.stereotype.Component;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.store.client.ClientStore;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class ClientStoreReactive implements ClientStore {
  private final WebClient client;

  public ClientStoreReactive(WebConfig config) {
    this.client = config.getDefaultStoreWebClient();
  }

  @Override
  public Mono<Void> sendHeadRequest(String uri) {
    return this.client.head()
        .uri(uri)
        .retrieve()
        .onStatus(
            status -> status.value() == 404,
            response -> Mono.error(() -> new NotFoundException("Resource not found at: " + response.request().getURI())))
        .toBodilessEntity()
        .map(ResponseEntity::getBody);
  }
}
