package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganizationDocument;
import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface DocumentController {
  Mono<TokenResponseDTO> createProvDocument(
      @NotNull @LoadOrganization Organization organization,
      @Valid @NotNull DocumentFormDTO body);

  Mono<DocumentResponseDTO> getFinalizedProvDocumentByIdentifier(
      @NotNull @LoadOrganizationDocument Organization organization);

  Mono<DocumentResponseDTO> getDomainProvDocumentByIdentifier(
      @NotNull @LoadOrganizationDocument Organization organization);

  Mono<DocumentResponseDTO> getBackboneProvDocumentByIdentifier(
      @NotNull @LoadOrganizationDocument Organization organization);

  Mono<Void> exists(
      @NotNull @LoadOrganizationDocument Organization organization);
}
