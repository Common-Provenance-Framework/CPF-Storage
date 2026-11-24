package org.commonprovenance.framework.store.web.store;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

/**
 * Provides reactive operations for checking storage resources and retrieving their tokens.
 */
public interface StoreWeb {

  /**
   * Checks whether a resource identified by its path is available in the storage service.
   *
   * @param resourcePath absolute resource URL or path
   * @return a publisher that completes if the resource exists, or emits an error otherwise
   */
  Mono<Void> pingByResourcePath(String resourcePath);

  /**
   * Retrieves the token associated with a resource identified by its path.
   *
   * @param resourcePath absolute resource URL or path
   * @return a publisher that emits the resource's token, or an error if it cannot be retrieved
   */
  Mono<Token> getTokenByResourcePath(String resourcePath);
}
