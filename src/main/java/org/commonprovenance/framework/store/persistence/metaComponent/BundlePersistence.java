package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.UUID;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.openprovenance.prov.model.Document;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface BundlePersistence {
  @NotNull
  Mono<Document> create(@NotNull Document bundle);

  @NotNull
  Mono<BundleNode> getById(@NotNull UUID id);

}
