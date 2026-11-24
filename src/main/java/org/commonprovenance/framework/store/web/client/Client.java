package org.commonprovenance.framework.store.web.client;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides configured {@link WebClient} instances and reactive helpers for sending HTTP requests to the storage service and trusted parties.
 *
 * <p>
 * Request methods return Reactor publishers, so requests are executed when those publishers are subscribed to. Query parameters may be {@code null} when no parameters are needed.
 */
public interface Client {
  /** Returns the configured {@link WebClient} for the storage service. */
  WebClient store();

  /** Returns the configured {@link WebClient} for the default trusted party. */
  WebClient trustedParty();

  /**
   * Returns a client for the supplied trusted-party base URL, or the default trusted-party client if no custom URL is present.
   *
   * @param customBaseUrl optional custom trusted-party base URL
   * @return the custom or default trusted-party client
   */
  WebClient trustedParty(Optional<String> customBaseUrl);

  /**
   * Creates a client using the supplied base URL and the configuration of the base client.
   *
   * @param baseUrl base URL for the client
   * @return a client configured with the supplied base URL
   */
  WebClient custom(String baseUrl);

  /**
   * Returns the configured default trusted-party base URL.
   *
   * @return the default trusted-party base URL
   */
  String getDefaultTrustedPartyUrl();

  /**
   * Returns the configured storage-service base URL.
   *
   * @return the storage-service base URL
   */
  String getStoreUrl();

  /**
   * Sends a HEAD request to the given URI.
   *
   * @param client      client used to send the request
   * @param uri         request URI, relative to the client's base URL
   * @param queryParams query parameters to include, or {@code null} if there are none
   * @return a publisher that completes when the response is received
   */
  Mono<Void> sendHeadRequest(WebClient client, String uri, Map<String, String> queryParams);

  /**
   * Sends a GET request and decodes the response body as one value of the given type.
   *
   * @param <T>          response body type
   * @param client       client used to send the request
   * @param uri          request URI, relative to the client's base URL
   * @param responseType class used to decode the response body
   * @param queryParams  query parameters to include, or {@code null} if there are none
   * @return a publisher of the decoded response body
   */
  <T> Mono<T> sendGetOneRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  /**
   * Sends a GET request and decodes the response body as a stream of values of the given type.
   *
   * @param <T>          response element type
   * @param client       client used to send the request
   * @param uri          request URI, relative to the client's base URL
   * @param responseType class used to decode each response element
   * @param queryParams  query parameters to include, or {@code null} if there are none
   * @return a publisher of decoded response elements
   */
  <T> Flux<T> sendGetManyRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  /**
   * Creates a function that sends a POST request with the supplied body when applied.
   *
   * @param <T>          response body type
   * @param <B>          request body type
   * @param client       client used to send the request
   * @param uri          request URI, relative to the client's base URL
   * @param responseType class used to decode the response body
   * @param queryParams  query parameters to include, or {@code null} if there are none
   * @return a function that accepts a request body and publishes the decoded response
   */
  <T, B> Function<B, Mono<T>> sendPostRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  /**
   * Creates a function that sends a PUT request with the supplied body when applied.
   *
   * @param <T>          response body type
   * @param <B>          request body type
   * @param client       client used to send the request
   * @param uri          request URI, relative to the client's base URL
   * @param responseType class used to decode the response body
   * @param queryParams  query parameters to include, or {@code null} if there are none
   * @return a function that accepts a request body and publishes the decoded response
   */
  <T, B> Function<B, Mono<T>> sendPutRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);

  /**
   * Sends a DELETE request and decodes the response body as the given type.
   *
   * @param <T>          response body type
   * @param client       client used to send the request
   * @param uri          request URI, relative to the client's base URL
   * @param responseType class used to decode the response body
   * @param queryParams  query parameters to include, or {@code null} if there are none
   * @return a publisher of the decoded response body
   */
  <T> Mono<T> sendDeleteRequest(WebClient client, String uri, Class<T> responseType, Map<String, String> queryParams);
}
