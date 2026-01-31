package org.commonprovenance.framework.store.web.trustedParty.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrustedPartyClient {
  <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType);

  public <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType);

  public <T, B> Mono<T> sendPostRequest(String uri, B body, Class<T> responseType);

  public <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType);
}
