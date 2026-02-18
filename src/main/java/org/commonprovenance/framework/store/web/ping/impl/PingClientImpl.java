package org.commonprovenance.framework.store.web.ping.impl;

import org.commonprovenance.framework.store.web.ping.PingClient;
import org.commonprovenance.framework.store.web.ping.client.Client;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class PingClientImpl implements PingClient {
  private final Client client;

  public PingClientImpl(Client client) {
    this.client = client;
  }

  @Override
  public @NotNull Mono<Void> pingByUrl(@NotNull String url) {
    return this.client.sendHeadRequest(url);
  }

}
