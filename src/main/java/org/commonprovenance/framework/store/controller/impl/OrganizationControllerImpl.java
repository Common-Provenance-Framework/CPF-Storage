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
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization and trusted-party management")
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

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping()
  @NotNull
  @Operation(summary = "Create organization")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Organization created"),
      @ApiResponse(responseCode = "409", description = "Organization already exists")
  })
  public Mono<OrganizationResponseDTO> createOrganization(
      @RequestBody OrganizationFormDTO body) {

    return ModelFactory.toDomain(body)
        .flatMap(MONO.makeSureAsync(
            this.organizationService::notExists,
            // this.trustedPartyWebService::notExists,
            "Organization with identifier '" + body.getIdentifier() + "' already exists!"))
        .flatMap(this.trustedPartyWebService.createOrganization(Optional.ofNullable(body.getTrustedPartyUri())))
        .flatMap(
            (Organization organization) -> Mono.justOrEmpty(organization.getTrustedParty())
                .flatMap(trustedParty -> this.trustedPartyService.findTrustedParty(trustedParty)
                    .onErrorResume(NotFoundException.class,
                        (NotFoundException notFound) -> trustedParty.getIsDefault()
                            ? this.trustedPartyService.storeTrustedParty(trustedParty)
                            : Mono.error(notFound)))
                .map(organization::withTrustedParty))
        .flatMap(this.organizationService::storeOrganization)
        .flatMap(DTOFactory::toDTO);
  }

  @PutMapping("/{identifier}")
  @NotNull
  @Operation(summary = "Update organization")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Organization updated"),
      @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public Mono<OrganizationResponseDTO> updateOrganization(
      @PathVariable String identifier,
      @RequestBody OrganizationFormDTO body) {
    return ModelFactory.toDomain(body)
        .flatMap((MONO.makeSureAsync(
            this.organizationService::exists,
            "Organization with identifier '" + body.getIdentifier() + "' does not exists!")))
        .flatMap(this.trustedPartyWebService::updateOrganization)
        .flatMap(this.organizationService::updateOrganization)
        .flatMap(DTOFactory::toDTO);
  }

  @GetMapping()
  @NotNull
  @Operation(summary = "List organizations")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Organizations fetched")
  })
  public Flux<OrganizationResponseDTO> getAllOrganizations() {
    return this.organizationService.getAllOrganizations()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{identifier}")
  @Operation(summary = "Get organization by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Organization fetched"),
      @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public Mono<OrganizationResponseDTO> getOrganizationByIdentifier(@PathVariable String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Identifier can not be null!").apply(identifier)
        .flatMap(this.organizationService::getOrganizationByIdentifier)
        .flatMap(DTOFactory::toDTO);
  }

}
