package org.commonprovenance.framework.store.controller;

import reactor.core.publisher.Mono;

public interface MetaDocumentController {
  Mono<Void> exists(String uuid);
}
