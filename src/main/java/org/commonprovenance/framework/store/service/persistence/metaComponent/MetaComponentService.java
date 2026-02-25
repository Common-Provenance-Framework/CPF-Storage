package org.commonprovenance.framework.store.service.persistence.metaComponent;

import org.openprovenance.prov.model.Document;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface MetaComponentService {
  @NotNull
  Mono<Document> storeMetaComponent(@NotNull Document document);

}
