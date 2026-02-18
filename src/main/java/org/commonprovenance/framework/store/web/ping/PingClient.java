package org.commonprovenance.framework.store.web.ping;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface PingClient {
  @NotNull
  Mono<Void> pingByUrl(@NotNull String url);
}
