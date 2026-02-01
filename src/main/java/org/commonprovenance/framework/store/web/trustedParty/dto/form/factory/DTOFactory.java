package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static OrganizationFormDTO fromModel(String name) {
    return new OrganizationFormDTO(name);
  }

  // ---

  public static Mono<OrganizationFormDTO> toForm(String name) {
    return MONO.<String>makeSureNotNullWithMessage("Organization name can not be null!").apply(name)
        .map(DTOFactory::fromModel);
  }
}
