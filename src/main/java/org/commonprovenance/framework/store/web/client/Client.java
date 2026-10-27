package org.commonprovenance.framework.store.web.client;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Client {
  WebClient store();

  WebClient trustedParty();

  /** Custom trusted party base url when present, otherwise the default trusted party client. */
  WebClient trustedParty(Optional<String> customBaseUrl);

  WebClient custom(String baseUrl);

  String getDefaultTrustedPartyUrl();

  String getStoreUrl();

  Mono<Void> sendHeadRequest(WebClient client, String uri, Map<String, String> queryParams);

  <T> Mono<T> sendGetOneRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  <T> Flux<T> sendGetManyRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  <T, B> Function<B, Mono<T>> sendPostRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  <T, B> Function<B, Mono<T>> sendPutRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  <T> Mono<T> sendDeleteRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);
}
