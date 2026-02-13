package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.validator.IsUUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationController {
  Mono<OrganizationResponseDTO> createOrganization(@Valid @NotNull OrganizationFormDTO body);

  Mono<OrganizationResponseDTO> updateOrganization(@IsUUID String uuid, @Valid @NotNull OrganizationFormDTO body);

  Flux<OrganizationResponseDTO> getAllOrganizations();

  Mono<OrganizationResponseDTO> getOrganizationById(@IsUUID String uuid);
}
