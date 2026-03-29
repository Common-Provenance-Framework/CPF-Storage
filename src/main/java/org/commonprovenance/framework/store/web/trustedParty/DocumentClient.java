package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Mono;

public interface DocumentClient {

  Mono<Document> getById(
      String organizationId,
      QualifiedName bundle_identifier,
      Format documentFormat,
      Optional<String> trustedPartyUrl);
}
