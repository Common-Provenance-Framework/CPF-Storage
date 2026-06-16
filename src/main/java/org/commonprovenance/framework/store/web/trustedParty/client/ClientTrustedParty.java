package org.commonprovenance.framework.store.web.trustedParty.client;

import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientTrustedParty {
  String getDefaultTrustedPartyUrl();

  <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType, java.util.Map<String, String> queryParams);

  <T> Function<String, Mono<T>> sendCustomGetOneRequest(
      String uri,
      Class<T> responseType,
      java.util.Map<String, String> queryParams);

  <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType, java.util.Map<String, String> queryParams);

  <T> Function<String, Flux<T>> sendCustomGetManyRequest(
      String uri,
      Class<T> responseType,
      java.util.Map<String, String> queryParams);

  <T, B> Function<B, Mono<T>> sendPostRequest(String uri, Class<T> responseType);

  <T, B> Function<String, Function<B, Mono<T>>> sendCustomPostRequest(String uri, Class<T> responseType);

  <T, B> Function<B, Mono<T>> sendPutRequest(String uri, Class<T> responseType);

  <T, B> Function<String, Function<B, Mono<T>>> sendCustomPutRequest(String uri, Class<T> responseType);

  <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType);

  <T> Function<String, Mono<T>> sendCustomDeleteRequest(String uri, Class<T> responseType);
}
