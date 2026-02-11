package org.commonprovenance.framework.store.web.trustedParty.client;

import java.util.function.Function;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Client {
  String getUrl();

  WebClient buildWebClient(String trustedPartyUrl);

  <T> Mono<T> sendGetOneRequest(String uri, Class<T> responseType);

  <T> Function<WebClient, Mono<T>> sendCustomGetOneRequest(String uri, Class<T> responseType);

  <T> Flux<T> sendGetManyRequest(String uri, Class<T> responseType);

  <T> Function<WebClient, Flux<T>> sendCustomGetManyRequest(String uri, Class<T> responseType);

  <T, B> Function<B, Mono<T>> sendPostRequest(String uri, Class<T> responseType);

  <T, B> Function<WebClient, Function<B, Mono<T>>> sendCustomPostRequest(String uri, Class<T> responseType);

  <T, B> Function<B, Mono<T>> sendPutRequest(String uri, Class<T> responseType);

  <T, B> Function<WebClient, Function<B, Mono<T>>> sendCustomPutRequest(String uri, Class<T> responseType);

  <T> Mono<T> sendDeleteRequest(String uri, Class<T> responseType);

  <T> Function<WebClient, Mono<T>> sendCustomDeleteRequest(String uri, Class<T> responseType);
}
