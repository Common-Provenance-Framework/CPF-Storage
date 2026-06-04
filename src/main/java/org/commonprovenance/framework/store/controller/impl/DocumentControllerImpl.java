package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.advice.ApplicationExceptionHandler;
import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.facade.DocumentFacade;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaProvenanceComponentService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.openprovenance.prov.model.ProvFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import io.swagger.v3.oas.annotations.Operation;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private final DocumentFacade documentFacade;

  private final DocumentService documentService;
  private final OrganizationService organizationService;
  private final TokenService tokenService;
  private final TrustedPartyService trustedPartyService;
  private final MetaProvenanceComponentService metaComponentService;

  private final TrustedPartyWebService trustedPartyWebService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  private final AppConfiguration configuration;

  public DocumentControllerImpl(
      DocumentFacade documentFacade,
      DocumentService documentService,
      OrganizationService organizationService,
      TokenService tokenService,
      TrustedPartyService trustedPartyService,
      MetaProvenanceComponentService metaComponentService,
      TrustedPartyWebService trustedPartyWebService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory,
      AppConfiguration configuration) {
    this.documentFacade = documentFacade;

    this.documentService = documentService;
    this.organizationService = organizationService;
    this.metaComponentService = metaComponentService;
    this.tokenService = tokenService;
    this.trustedPartyService = trustedPartyService;
    this.trustedPartyWebService = trustedPartyWebService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;

    this.configuration = configuration;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @NotNull
  @Operation(summary = "Create a provenance document")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Document created"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "409", description = "Conflict with existing data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BadRequestDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<TokenResponseDTO> createProvDocument(
      @PathVariable String organizationIdentifier,
      @RequestBody DocumentFormDTO body) {
    System.out.println(organizationIdentifier);
    return this.documentFacade.createProvDocument(organizationIdentifier, body);

  }

  // @NotNull
  // @GetMapping("/{identifier}")
  // @Operation(summary = "Get provenance document by identifier")
  // @ApiResponses({
  // @ApiResponse(responseCode = "200", description = "Document fetched"),
  // @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // NotFoundDTO.class))),
  // @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // InternalServerErrorDTO.class)))
  // })
  // public Mono<DocumentResponseDTO> getFinalizedProvDocumentByIdentifier(@PathVariable String identifier) {
  // return Mono.justOrEmpty(identifier)
  // .flatMap(this.tokenService::getByDocumentIdentifier)
  // .flatMap(DTOFactory::toDocumentDTO);
  // }

  // @NotNull
  // @GetMapping("/{identifier}/domain-specific")
  // @Operation(summary = "Get domain specific provenance document by identifier")
  // @ApiResponses({
  // @ApiResponse(responseCode = "200", description = "Document fetched"),
  // @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // NotFoundDTO.class))),
  // @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // InternalServerErrorDTO.class)))
  // })
  // public Mono<DocumentResponseDTO> getDomainProvDocumentByIdentifier(@PathVariable String identifier) {
  // return Mono.justOrEmpty(identifier)
  // .flatMap(this.documentService::getDocumentByIdentifier)
  // .flatMap(MONO.liftEffectToMono(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory)))
  // .flatMap(document -> Mono.justOrEmpty(document.getCpmDocument())
  // .switchIfEmpty(Mono.error(new NotFoundException(
  // "Finalized provenance document for identifier '" + identifier + "' can not be deserialized.")))
  // .map(cpm -> new CpmDocument(
  // cpm.getBundleId(),
  // Collections.emptyList(),
  // cpm.getDomainSpecificPart(),
  // Collections.emptyList(),
  // this.provFactory,
  // this.cpmProvFactory,
  // this.cpmFactory))
  // .flatMap(MONO.liftEffectToMono(DocumentUtils.serialize(Formats.ProvFormat.JSON)))
  // .flatMap(MONO.liftEffectToMono(Base64Utils::encodeFromString))
  // .flatMap(MONO.liftEffectToMono(cpmStr -> document
  // .withGraph(cpmStr)
  // .withCpmDocument(provFactory, cpmProvFactory, cpmFactory)))
  // .flatMap(provDoc -> Mono.justOrEmpty(provDoc.getIdentifier())
  // .flatMap(this.documentService::getOrganizationIdentifierByIdentifier)
  // .map(provDoc::withOrganizationIdentifier))
  // .flatMap(provDoc -> Mono.justOrEmpty(provDoc)
  // .map(Document::getOrganizationIdentifier)
  // .flatMap(this.trustedPartyService::getTrustedPartyUrlByOrganizationIdentifier)
  // .map(Optional::ofNullable)
  // .flatMap(optUrl -> this.trustedPartyWebService.issueDomainSpecificGraphToken(optUrl).apply(provDoc))
  // .map(token -> token.withDocument(provDoc)))
  // .flatMap(token -> Mono.justOrEmpty(token.getToken().getOrganizationIdentifier())
  // .flatMap(this.trustedPartyService::getTrustedPartyByOrganizationIdentifier)
  // .map(token::withTrustedParty)))
  // .flatMap(DTOFactory::toDocumentDTO);
  // }

  // @NotNull
  // @GetMapping("/{identifier}/backbone")
  // @Operation(summary = "Get backbone provenance document by identifier")
  // @ApiResponses({
  // @ApiResponse(responseCode = "200", description = "Document fetched"),
  // @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // NotFoundDTO.class))),
  // @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation =
  // InternalServerErrorDTO.class)))
  // })
  // public Mono<DocumentResponseDTO> getBackboneProvDocumentByIdentifier(@PathVariable String identifier) {
  // return Mono.justOrEmpty(identifier)
  // .flatMap(this.documentService::getDocumentByIdentifier)
  // .flatMap(MONO.liftEffectToMono(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory)))
  // .flatMap(document -> Mono.justOrEmpty(document.getCpmDocument())
  // .switchIfEmpty(Mono.error(new NotFoundException(
  // "Finalized provenance document for identifier '" + identifier + "' can not be deserialized.")))
  // .map(cpm -> new CpmDocument(
  // cpm.getBundleId(),
  // cpm.getTraversalInformationPart(),
  // Collections.emptyList(),
  // Collections.emptyList(),
  // this.provFactory,
  // this.cpmProvFactory,
  // this.cpmFactory))
  // .flatMap(MONO.liftEffectToMono(DocumentUtils.serialize(Formats.ProvFormat.JSON)))
  // .flatMap(MONO.liftEffectToMono(Base64Utils::encodeFromString))
  // .flatMap(MONO.liftEffectToMono(cpmStr -> document
  // .withGraph(cpmStr)
  // .withCpmDocument(provFactory, cpmProvFactory, cpmFactory)))
  // .flatMap(provDoc -> Mono.justOrEmpty(provDoc.getIdentifier())
  // .flatMap(this.documentService::getOrganizationIdentifierByIdentifier)
  // .map(provDoc::withOrganizationIdentifier))
  // .flatMap(provDoc -> Mono.justOrEmpty(provDoc)
  // .map(Document::getOrganizationIdentifier)
  // .flatMap(this.trustedPartyService::getTrustedPartyUrlByOrganizationIdentifier)
  // .map(Optional::ofNullable)
  // .flatMap(optUrl -> this.trustedPartyWebService.issueDomainSpecificGraphToken(optUrl).apply(provDoc))
  // .map(token -> token.withDocument(provDoc)))
  // .flatMap(token -> Mono.justOrEmpty(token.getToken().getOrganizationIdentifier())
  // .flatMap(this.trustedPartyService::getTrustedPartyByOrganizationIdentifier)
  // .map(token::withTrustedParty)))
  // .flatMap(DTOFactory::toDocumentDTO);
  // }

  @Override
  @NotNull
  @RequestMapping(path = "/{identifier}", method = RequestMethod.HEAD)
  @Operation(summary = "Check if a document exists")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document exists"),
      @ApiResponse(responseCode = "404", description = "Document does not exist"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Mono<Void> exists(@PathVariable String organizationIdentifier, @PathVariable String identifier) {
    return Mono.just(identifier)
        .flatMap(MONO.makeSureNotNull(new BadRequestException("Path variable document identifier can not be null!")))
        .flatMap(this.documentFacade::exists);
  }
}
