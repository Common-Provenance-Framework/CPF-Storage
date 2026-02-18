package org.commonprovenance.framework.store.service.web.store;

import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Mono;

public interface StoreWebService {
  Mono<Boolean> pingUrl(String url);

  Mono<Boolean> pingQualifiedName(QualifiedName qn);
}
