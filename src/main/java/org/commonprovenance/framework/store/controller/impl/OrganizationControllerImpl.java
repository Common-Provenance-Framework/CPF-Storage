package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;

import org.commonprovenance.framework.store.controller.OrganizationController;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.TrustedPartyService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/organizations")
public class OrganizationControllerImpl implements OrganizationController {
  private final OrganizationService organizationService;
  private final TrustedPartyService trustedPartyService;

  private final TrustedPartyWebService trustedPartyWebService;

  public OrganizationControllerImpl(
      OrganizationService organizationService,
      TrustedPartyService trustedPartyService,
      TrustedPartyWebService trustedPartyWebService) {
    this.organizationService = organizationService;
    this.trustedPartyService = trustedPartyService;

    this.trustedPartyWebService = trustedPartyWebService;
  }

  @PostMapping()
  @NotNull
  public Mono<OrganizationResponseDTO> createOrganization(
      @RequestBody OrganizationFormDTO body) {

    return ModelFactory.toDomain(body)
        .flatMap(MONO.makeSureAsync(
            this.organizationService::notExists,
            // this.trustedPartyWebService::notExists,
            "Organization with name '" + body.getName() + "' already exists!"))
        .flatMap(this.trustedPartyWebService.createOrganization(Optional.ofNullable(body.getTrustedPartyUri())))
        .flatMap(
            (Organization organization) -> this.trustedPartyService.findTrustedParty(organization.getTrustedParty())
                .onErrorResume(NotFoundException.class,
                    (NotFoundException notFound) -> organization.getTrustedParty().getIsDefault()
                        ? Mono.just(organization.getTrustedParty())
                            .flatMap(this.trustedPartyService::storeTrustedParty)
                        : Mono.error(notFound))
                .map(organization::withTrustedParty))
        .flatMap(this.organizationService::storeOrganization)
        .flatMap(DTOFactory::toDTO);
  }

  @PutMapping("/{uuid}")
  @NotNull
  public Mono<OrganizationResponseDTO> updateOrganization(
      @PathVariable String uuid,
      @RequestBody OrganizationFormDTO body) {
    return ModelFactory.toDomain(body)
        .flatMap((Organization organization) -> ModelFactory.toUUID(uuid).map(organization::withId))
        .flatMap((MONO.makeSureAsync(
            this.organizationService::exists,
            "Organization with name '" + body.getName() + "' does not exists!")))
        .flatMap(this.trustedPartyWebService::updateOrganization)
        .flatMap(this.organizationService::updateOrganization)
        .flatMap(DTOFactory::toDTO);
  }

  @GetMapping()
  @NotNull
  public Flux<OrganizationResponseDTO> getAllOrganizations() {
    return this.organizationService.getAllOrganizations()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<OrganizationResponseDTO> getOrganizationById(@PathVariable String uuid) {
    return ModelFactory.toUUID(uuid)
        .flatMap(this.organizationService::getOrganizationById)
        .flatMap(DTOFactory::toDTO);
  }

}
