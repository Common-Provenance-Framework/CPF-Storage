package org.commonprovenance.framework.store.controller.facade;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationRegisterFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationUpdateFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface OrganizationFacade {
  Mono<OrganizationResponseDTO> register(OrganizationRegisterFormDTO body);

  Mono<OrganizationResponseDTO> update(Organization organization, OrganizationUpdateFormDTO body);

  Mono<OrganizationResponseDTO> getOrganizationByIdentifier(Organization organization);

}
