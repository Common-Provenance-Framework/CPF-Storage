package org.commonprovenance.framework.store.web.store;

import reactor.core.publisher.Mono;

public interface PingClient {

  Mono<Void> pingByUrl(String url);
}
