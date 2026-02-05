package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationTPFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static OrganizationTPFormDTO fromModel(Organization model) {
    return new OrganizationTPFormDTO(
        model.getName(),
        model.getClientCertificate(),
        model.getIntermediateCertificates());
  }

  // ---

  public static Mono<OrganizationTPFormDTO> toForm(Organization model) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization model can not be null!").apply(model)
        .map(DTOFactory::fromModel);
  }
}
