package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationController {
  Mono<OrganizationResponseDTO> createOrganization(OrganizationFormDTO body);

  Mono<OrganizationResponseDTO> updateOrganization(String uuid, OrganizationFormDTO body);

  Flux<OrganizationResponseDTO> getAllOrganizations();

  Mono<OrganizationResponseDTO> getOrganizationById(String uuid);
}
