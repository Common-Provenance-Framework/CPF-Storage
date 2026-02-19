package org.commonprovenance.framework.store.web.store.client;

import reactor.core.publisher.Mono;

public interface ClientStore {
  Mono<Void> sendHeadRequest(String url);
}
