package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationTPFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static OrganizationTPFormDTO fromModel(OrganizationFormDTO form) {
    return new OrganizationTPFormDTO(
        form.getName(),
        form.getClientCertificate(),
        form.getIntermediateCertificates());
  }

  // ---

  public static Mono<OrganizationTPFormDTO> toForm(OrganizationFormDTO form) {
    return MONO.<OrganizationFormDTO>makeSureNotNullWithMessage("Organization formular can not be null!").apply(form)
        .map(DTOFactory::fromModel);
  }
}
