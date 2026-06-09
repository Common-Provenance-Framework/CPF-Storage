package org.commonprovenance.framework.store.controller.facade;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;

import reactor.core.publisher.Mono;

public interface OrganizationFacade {
  Mono<OrganizationResponseDTO> register(OrganizationFormDTO body);

  Mono<OrganizationResponseDTO> update(OrganizationFormDTO body);

  Mono<OrganizationResponseDTO> getOrganizationByIdentifier(String identifier);

}
