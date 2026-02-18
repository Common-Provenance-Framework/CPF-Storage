package org.commonprovenance.framework.store.service.web.ping;

import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Mono;

public interface PingWebService {
  Mono<Boolean> pingUrl(String url);

  Mono<Boolean> pingQualifiedName(QualifiedName qn);
}
