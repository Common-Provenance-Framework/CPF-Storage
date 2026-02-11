package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TokenClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TokenClientImpl implements TokenClient {
  private final Client client;

  public TokenClientImpl(
      Client client) {
    this.client = client;
  }

  private String getTokensUri(String id) {
    return "/organizations/" + id + "/tokens";
  }

  @Override
  public @NotNull Function<String, Flux<Token>> getAllByOrganization(Optional<String> trustedPartyUrl) {
    return (@NotNull String organizationId) -> Mono.just(organizationId)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .flatMapMany((String orgId) -> trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomGetManyRequest(getTokensUri(orgId), TokenTPResponseDTO.class))
            .orElse(this.client.sendGetManyRequest(getTokensUri(orgId), TokenTPResponseDTO.class)))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Token> getByDocumentId(
      @NotNull String organizationId,
      @NotNull QualifiedName bundle_identifier,
      @NotNull Format documentFormat,
      Optional<String> trustedPartyUrl) {
    String uri = getTokensUri(organizationId) + "/" + bundle_identifier.getUri() + "/" + documentFormat.toString();

    return trustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetOneRequest(uri, TokenTPResponseDTO.class))
        .orElse(client.sendGetOneRequest(uri, TokenTPResponseDTO.class))
        .flatMap(ModelFactory::toDomain);
  }
}
