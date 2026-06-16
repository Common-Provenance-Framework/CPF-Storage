package org.commonprovenance.framework.store.web.store;

import reactor.core.publisher.Mono;

public interface StoreWeb {

  Mono<Void> pingByResourcePath(String resourcePath);
}
