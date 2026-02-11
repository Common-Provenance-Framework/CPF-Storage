package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.openprovenance.prov.model.QualifiedName;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface DocumentClient {
  @NotNull
  Mono<Document> getById(
      @NotNull String organizationId,
      @NotNull QualifiedName bundle_identifier,
      @NotNull Format documentFormat,
      Optional<String> trustedPartyUrl);
}
