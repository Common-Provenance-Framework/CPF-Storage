package org.commonprovenance.framework.store.service.persistence.metaComponent;

import java.util.function.Function;

import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Mono;

public interface MetaComponentService {

  Mono<Document> createMetaComponent(QualifiedName metaBundleIdentifier);

  Function<Document, Mono<Document>> addNewVersion(QualifiedName identifier);

  Function<Document, Mono<Document>> addTokenToLastVersion(Token token);

  Mono<Document> getMetaComponent(QualifiedName metaBundleIdentifier);

  Mono<Document> getByIdentifier(QualifiedName metaBundleIdentifier);

  Mono<Boolean> exists(String identifier);

}
