package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Service
public class MetaComponentServiceImpl implements MetaComponentService {
  private final BundlePersistence persistence;

  public MetaComponentServiceImpl(BundlePersistence persistence) {
    this.persistence = persistence;
  }

  @Override
  public @NotNull Mono<Document> storeMetaComponent(@NotNull Document document) {
    return this.persistence.create(document);
  }

}