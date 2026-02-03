package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.controller.validator.IsUUID;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.OrganizationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
  private final OrganizationService service;

  public OrganizationController(OrganizationService service) {
    this.service = service;
  }

  @PostMapping()
  @NotNull
  public Mono<OrganizationResponseDTO> createOrganization(@Valid @RequestBody @NotNull OrganizationFormDTO body) {
    return ModelFactory.toDomain(body)
        .flatMap(this.service::storeOrganization)
        .flatMap(DTOFactory::toDTO);
  }

  @GetMapping()
  @NotNull
  public Flux<OrganizationResponseDTO> getAllOrganizations() {
    return this.service.getAllOrganizations()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<OrganizationResponseDTO> getOrganizationById(@PathVariable @IsUUID String uuid) {
    return ModelFactory.toUUID(uuid)
        .flatMap(this.service::getOrganizationById)
        .flatMap(DTOFactory::toDTO);
  }
}
