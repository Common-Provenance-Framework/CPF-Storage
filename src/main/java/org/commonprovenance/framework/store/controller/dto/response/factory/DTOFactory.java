package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static TokenResponseDTO fromModelToken(Token model) {
    return new TokenResponseDTO(model.getJwt());
  }

  private static DocumentResponseDTO fromModel(Token model) {
    return new DocumentResponseDTO(
        model.getDocument().getGraph(),
        DTOFactory.fromModelToken(model));
  }

  private static OrganizationResponseDTO fromModel(Organization model) {
    return new OrganizationResponseDTO(
        model.getIdentifier(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());
  }

  public static Mono<DocumentResponseDTO> toDocumentDTO(Token token) {
    return MONO.makeSureNotNull(token)
        .map(DTOFactory::fromModel);
  }

  public static Mono<TokenResponseDTO> toTokenDTO(Token token) {
    return MONO.makeSureNotNull(token)
        .map(DTOFactory::fromModelToken);
  }

  public static Mono<OrganizationResponseDTO> toDTO(Organization organization) {
    return MONO.makeSureNotNull(organization)
        .map(DTOFactory::fromModel);
  }
}
