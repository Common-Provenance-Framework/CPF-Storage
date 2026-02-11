package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.DocumentClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class DocumentClientImpl implements DocumentClient {
  private final Client client;

  public DocumentClientImpl(
      Client client) {
    this.client = client;
  }

  @Override
  public @NotNull Mono<Document> getById(
      @NotNull String organizationId,
      @NotNull QualifiedName bundle_identifier,
      @NotNull Format documentFormat,
      Optional<String> trustedPartyUrl) {
    String uri = "organizations/" + organizationId + "/documents/" + bundle_identifier.getUri() + "/"
        + documentFormat.toString();
    return trustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetOneRequest(uri, DocumentTPResponseDTO.class))
        .orElse(this.client.sendGetOneRequest(uri, DocumentTPResponseDTO.class))
        .flatMap(ModelFactory::toDomain);
  }

}
