package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.DocumentClient;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTP;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class DocumentClientImpl implements DocumentClient {
  private final ClientTP client;

  public DocumentClientImpl(
      ClientTP client) {
    this.client = client;
  }

  @Override
  public Mono<Document> getById(
      String organizationId,
      QualifiedName bundle_identifier,
      Format documentFormat,
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
