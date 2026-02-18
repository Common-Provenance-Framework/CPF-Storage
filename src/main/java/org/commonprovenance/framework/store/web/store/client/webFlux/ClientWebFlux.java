package org.commonprovenance.framework.store.web.store.client.webFlux;

import org.springframework.stereotype.Component;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.web.store.client.Client;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@Profile("live & webflux")
public class ClientWebFlux implements Client {
  public ClientWebFlux() {
  }

  @Override
  public Mono<Void> sendHeadRequest(String url) {
    return WebClient.builder()
        .baseUrl(url)
        .build()
        .head()
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new NotFoundException("Resource not found at: " + url)))
        .bodyToMono(Void.class);
  }
}
