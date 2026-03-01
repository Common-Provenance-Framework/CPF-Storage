package org.commonprovenance.framework.store.service.persistence.metaComponent;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.QualifiedName;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface MetaComponentService {
  @NotNull
  Mono<Document> storeMetaComponent(@NotNull Document document);

  @NotNull
  Mono<Document> createMetaComponent(@NotNull QualifiedName metaBundleId);

  @NotNull
  Function<Document, Mono<Document>> addNewVersion(QualifiedName identifier);

  @NotNull
  Function<Document, Mono<Document>> addTokenToLastVersion(Token tokenModel);

  @NotNull
  Mono<Document> getMetaComponent(@NotNull QualifiedName metaBundleId);

  @NotNull
  Mono<Document> getById(@NotNull QualifiedName metaBundleId);

  @NotNull
  Mono<Boolean> exists(@NotNull String id);

}
