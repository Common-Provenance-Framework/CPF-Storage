package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.QualifiedName;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenClient {
  @NotNull
  Flux<Token> getAllByOrganization(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl);

  @NotNull
  Mono<Token> getByDocumentId(
      @NotNull String organizationId,
      @NotNull QualifiedName bundle_identifier,
      @NotNull Format documentFormat,
      Optional<String> trustedPartyUrl);

}
