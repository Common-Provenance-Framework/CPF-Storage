package org.commonprovenance.framework.store.web.store.impl;

import org.commonprovenance.framework.store.web.store.PingClient;
import org.commonprovenance.framework.store.web.store.client.ClientStore;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class PingClientImpl implements PingClient {
  private final ClientStore client;

  public PingClientImpl(ClientStore client) {
    this.client = client;
  }

  @Override
  public @NotNull Mono<Void> pingByUrl(@NotNull String url) {
    return this.client.sendHeadRequest(url);
  }

}
