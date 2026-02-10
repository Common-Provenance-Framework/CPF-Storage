package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

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

  private Flux<TokenTPResponseDTO> getManyReq(String organizationId) {
    return client.sendGetManyRequest("/organizations/" + organizationId + "/tokens", TokenTPResponseDTO.class);
  }

  @Override
  public @NotNull Flux<Token> getAllByOrganization(@NotNull String organizationId) {
    return Mono.just(organizationId)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .flatMapMany(this::getManyReq)
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Token> getByDocumentId(
      @NotNull String organizationId,
      @NotNull QualifiedName bundle_identifier,
      @NotNull Format documentFormat) {
    return client.sendGetOneRequest(
        "organizations/"
            + organizationId
            + "/tokens/"
            + bundle_identifier.getUri()
            + "/"
            + documentFormat.toString(),
        TokenTPResponseDTO.class)
        .flatMap(ModelFactory::toDomain);
  }
}
