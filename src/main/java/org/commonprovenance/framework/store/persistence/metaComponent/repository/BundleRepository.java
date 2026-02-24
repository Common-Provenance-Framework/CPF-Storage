package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;

import reactor.core.publisher.Mono;

public interface BundleRepository {
  Mono<BundleNode> save(BundleNode bundle);

  Mono<BundleNode> findById(String id);
}
