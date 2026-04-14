package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TokenClient;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTP;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TokenClientImpl implements TokenClient {
  private final ClientTP client;

  public TokenClientImpl(
      ClientTP client) {
    this.client = client;
  }

  private String getTokensUri(String id) {
    return "/organizations/" + id + "/tokens";
  }

  @Override
  public Function<String, Flux<Token>> getAllByOrganization(Optional<String> trustedPartyUrl) {
    return (String organizationId) -> Mono.just(organizationId)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .flatMapMany((String orgId) -> {
          String uri = getTokensUri(orgId);
          Map<String, String> queryParams = Map.of("tokenFormat", "jwt");

          return trustedPartyUrl
              .map(this.client::buildWebClient)
              .map(this.client.sendCustomGetManyRequest(uri, TokenTPResponseDTO.class, queryParams))
              .orElse(this.client.sendGetManyRequest(uri, TokenTPResponseDTO.class, queryParams));
        })
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Token> getByDocumentId(
      String organizationId,
      QualifiedName bundle_identifier,
      Format documentFormat,
      Optional<String> trustedPartyUrl) {
    String uri = getTokensUri(organizationId) + "/" + bundle_identifier.getUri() + "/" + documentFormat.toString();
    Map<String, String> queryParams = Map.of("tokenFormat", "jwt");

    return trustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetOneRequest(uri, TokenTPResponseDTO.class, queryParams))
        .orElse(client.sendGetOneRequest(uri, TokenTPResponseDTO.class, queryParams))
        .flatMap(ModelFactory::toDomain);
  }
}
