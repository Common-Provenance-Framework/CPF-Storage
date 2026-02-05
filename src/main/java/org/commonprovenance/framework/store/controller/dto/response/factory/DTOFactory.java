package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static DocumentResponseDTO fromModel(Document model) {
    return new DocumentResponseDTO(
        model.getId().map(UUID::toString).orElse(null),
        model.getGraph(),
        model.getFormat().map(Format::toString).orElse(null));
  }

  private static OrganizationResponseDTO fromModel(Organization model) {
    return new OrganizationResponseDTO(
        model.getId().map(UUID::toString).orElse(null),
        model.getName(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());
  }

  // ---

  public static Mono<DocumentResponseDTO> toDTO(Document document) {
    return MONO.makeSureNotNull(document)
        .map(DTOFactory::fromModel);
  }

  public static Mono<OrganizationResponseDTO> toDTO(Organization organization) {
    return MONO.makeSureNotNull(organization)
        .map(DTOFactory::fromModel);
  }
}
