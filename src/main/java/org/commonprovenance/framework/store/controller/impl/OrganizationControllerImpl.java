package org.commonprovenance.framework.store.controller.impl;

import org.commonprovenance.framework.store.controller.OrganizationController;
import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationRegisterFormDTO;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationUpdateFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.controller.facade.OrganizationFacade;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.model.Organization;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping(path = "/api/v1/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organizations", description = "Organization and trusted-party management")
public class OrganizationControllerImpl implements OrganizationController {
  private final OrganizationFacade organizationFacade;

  public OrganizationControllerImpl(OrganizationFacade organizationFacade) {
    this.organizationFacade = organizationFacade;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @NotNull
  @Operation(summary = "Create organization")
  @ApiResponses({ @ApiResponse(responseCode = "201", description = "Organization created"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "409", description = "Organization already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class))) })
  public Mono<OrganizationResponseDTO> createOrganization(
      @RequestBody OrganizationRegisterFormDTO body) {
    return organizationFacade.register(body);
  }

  @PutMapping(path = "/{identifier}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @NotNull
  @Operation(summary = "Update organization", parameters = { @Parameter(name = "identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")) })
  @ApiResponses({ @ApiResponse(responseCode = "200", description = "Organization updated"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "404", description = "Organization not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class))) })

  public Mono<OrganizationResponseDTO> updateOrganization(
      @Parameter(hidden = true) @LoadOrganization(value = "identifier") Organization organization,
      @RequestBody OrganizationUpdateFormDTO body) {
    return organizationFacade.update(organization, body);
  }

  @NotNull
  @GetMapping("/{identifier}")
  @Operation(summary = "Get organization by identifier", parameters = {
      @Parameter(name = "identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")) })
  @ApiResponses({ @ApiResponse(responseCode = "200", description = "Organization fetched"),
      @ApiResponse(responseCode = "404", description = "Organization not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class))) })
  public Mono<OrganizationResponseDTO> getOrganizationByIdentifier(
      @Parameter(hidden = true) @LoadOrganization(value = "identifier") Organization organization) {
    return organizationFacade.getOrganizationByIdentifier(organization);
  }

}
