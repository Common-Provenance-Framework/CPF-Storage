package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.UUID;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface BundlePersistence {
  @NotNull
  Mono<BundleNode> create(@NotNull BundleNode bundle);

  @NotNull
  Mono<BundleNode> getById(@NotNull UUID id);

}
