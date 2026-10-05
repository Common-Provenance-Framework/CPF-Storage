package org.commonprovenance.framework.store.web.store;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

public interface StoreWeb {

  Mono<Void> pingByResourcePath(String resourcePath);

  Mono<Token> getTokenByResourcePath(String resourcePath);
}
