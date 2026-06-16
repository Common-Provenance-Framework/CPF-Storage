package org.commonprovenance.framework.store.controller.impl;

import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.facade.DocumentFacade;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganizationDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping(path = "/api/v1/organizations/{organizationIdentifier}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Documents", description = "Operations for storing and reading provenance documents")
public class DocumentControllerImpl implements DocumentController {
  private final DocumentFacade documentFacade;

  public DocumentControllerImpl(
      DocumentFacade documentFacade) {
    this.documentFacade = documentFacade;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @NotNull
  @Operation(summary = "Create a provenance document", parameters = {
      @Parameter(name = "organizationIdentifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")) })
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Document created"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "409", description = "Conflict with existing data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  @Override
  public Mono<TokenResponseDTO> createProvDocument(
      @Parameter(hidden = true) @LoadOrganization(value = "organizationIdentifier") Organization organization,
      @RequestBody DocumentFormDTO body) {
    return this.documentFacade.createProvDocument(organization, body);

  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{identifier}")
  @NotNull
  @Operation(summary = "Get provenance document by identifier", parameters = {
      @Parameter(name = "organizationIdentifier", description = "Organization identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
      @Parameter(name = "identifier", description = "Document identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string"))
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  @Override
  public Mono<DocumentResponseDTO> getFinalizedProvDocumentByIdentifier(
      @Parameter(hidden = true) @LoadOrganizationDocument() Organization organization) {
    return Mono.just(organization)
        .flatMap(this.documentFacade::getProvDocument);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{identifier}/domain-specific")
  @NotNull
  @Operation(summary = "Get domain specific provenance document by identifier", parameters = {
      @Parameter(name = "organizationIdentifier", description = "Organization identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
      @Parameter(name = "identifier", description = "Document identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string"))
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  @Override
  public Mono<DocumentResponseDTO> getDomainProvDocumentByIdentifier(
      @Parameter(hidden = true) @LoadOrganizationDocument() Organization organization) {
    return Mono.just(organization)
        .flatMap(this.documentFacade::getDomainProvDocument);
  }

  @NotNull
  @GetMapping("/{identifier}/backbone")
  @Operation(summary = "Get backbone provenance document by identifier", parameters = {
      @Parameter(name = "organizationIdentifier", description = "Organization identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
      @Parameter(name = "identifier", description = "Document identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string"))
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  @Override
  public Mono<DocumentResponseDTO> getBackboneProvDocumentByIdentifier(
      @Parameter(hidden = true) @LoadOrganizationDocument() Organization organization) {
    return Mono.just(organization)
        .flatMap(this.documentFacade::getBackboneProvDocument);
  }

  @NotNull
  @RequestMapping(path = "/{identifier}", method = RequestMethod.HEAD)
  @Operation(summary = "Check if a document exists", parameters = {
      @Parameter(name = "organizationIdentifier", description = "Organization identifier.", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
      @Parameter(name = "identifier", description = "Document identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string"))
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document exists"),
      @ApiResponse(responseCode = "404", description = "Document does not exist"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Override
  public Mono<Void> exists(
      @Parameter(hidden = true) @LoadOrganizationDocument(organizationIdentifier = "organizationIdentifier", documentIdentifier = "identifier") Organization organization) {
    return Mono.just(organization).then();
  }
}
