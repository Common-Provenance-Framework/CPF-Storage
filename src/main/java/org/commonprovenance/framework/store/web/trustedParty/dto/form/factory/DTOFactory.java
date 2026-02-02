package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static OrganizationFormDTO fromModel(
      org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO form) {
    return new OrganizationFormDTO(
        form.getName(),
        form.getClientCertificate(),
        form.getIntermediateCertificates());
  }

  // ---

  public static Mono<OrganizationFormDTO> toForm(
      org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO form) {
    return MONO.<org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO>makeSureNotNullWithMessage(
        "Organization formular can not be null!").apply(form)
        .map(DTOFactory::fromModel);
  }
}
