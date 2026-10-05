package org.commonprovenance.framework.store.web.store.client;

import java.util.function.Function;

import reactor.core.publisher.Mono;

public interface ClientStore {
  Mono<Void> sendHeadRequest(String uri);

  <T> Function<String, Mono<T>> sendGetOneRequest(Class<T> responseType);
}
