package org.commonprovenance.framework.store.web.ping.client;

import reactor.core.publisher.Mono;

public interface Client {
  Mono<Void> sendHeadRequest(String url);
}
