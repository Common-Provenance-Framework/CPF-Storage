package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationRegisterFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationUpdateFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface OrganizationController {
  Mono<OrganizationResponseDTO> createOrganization(
      @Valid @NotNull OrganizationRegisterFormDTO body);

  Mono<OrganizationResponseDTO> updateOrganization(
      @NotNull @LoadOrganization Organization organization,
      @Valid @NotNull OrganizationUpdateFormDTO body);

  Mono<OrganizationResponseDTO> getOrganizationByIdentifier(
      @NotNull @LoadOrganization Organization organization);
}
