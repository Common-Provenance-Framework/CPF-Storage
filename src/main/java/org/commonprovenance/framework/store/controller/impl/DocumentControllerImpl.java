package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.CpmDocumentUtils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.interop.Formats;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.model.INode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping(path = "/api/v1/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Documents", description = "Operations for storing and reading provenance documents")
public class DocumentControllerImpl implements DocumentController {
  private final DocumentService documentService;
  private final OrganizationService organizationService;
  private final TokenService tokenService;
  private final TrustedPartyService trustedPartyService;
  private final MetaComponentService metaComponentService;

  private final TrustedPartyWebService trustedPartyWebService;
  private final StoreWebService storeWebService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  private final AppConfiguration configuration;

  public DocumentControllerImpl(
      DocumentService documentService,
      OrganizationService organizationService,
      TokenService tokenService,
      TrustedPartyService trustedPartyService,
      MetaComponentService metaComponentService,
      TrustedPartyWebService trustedPartyWebService,
      StoreWebService storeWebService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory,
      AppConfiguration configuration) {
    this.documentService = documentService;
    this.organizationService = organizationService;
    this.metaComponentService = metaComponentService;
    this.tokenService = tokenService;
    this.trustedPartyService = trustedPartyService;
    this.trustedPartyWebService = trustedPartyWebService;
    this.storeWebService = storeWebService;

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
      @RequestBody DocumentFormDTO body) {
    return ModelFactory.toDomain(body)
        // validate Organization and TrustedParty first
        .delayUntil((Document document) -> Mono.just(new Organization())
            .map(organization -> organization.withIdentifier(document.getOrganizationIdentifier()))
            .flatMap(MONO.makeSureAsync(
                this.organizationService::exists,
                org -> new ConflictException(
                    "Organization with identifier " + org.getIdentifier() + " has not been registered yet!")))
            .flatMap(organization -> Mono.justOrEmpty(organization.getIdentifier())
                .flatMap(this.organizationService::getOrganizationByIdentifier))
            .map(organization -> organization.getTrustedParty())
            .flatMap(Mono::justOrEmpty)
            .flatMap(MONO.makeSure(
                tp -> tp.getIsChecked(),
                _ -> new ConflictException(
                    "Trusted party has not been checked for its validity yet!")))
            .flatMap(MONO.makeSure(
                tp -> tp.getIsValid(),
                _ -> new ConflictException(
                    "Trusted party has been checked, but has not been considered as vaid!"))))
        // --------------------------
        // validate document signature
        .delayUntil((Document document) -> Mono.justOrEmpty(document.getOrganizationIdentifier())
            .flatMap(this.organizationService::getOrganizationByIdentifier)
            .flatMap(MONO.makeSureAsync(
                trustedPartyWebService.verifySignature(document),
                _ -> new BadRequestException("Invalid signature!"))))
        // --------------------------
        // deserialize document into CpmDocument class
        .map(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory))
        .flatMap(MONO.makeSure(
            doc -> doc.getCpmDocument().isPresent(),
            doc -> new InternalApplicationException("Graf has not been deserialized")))
        // --------------------------
        // get document id from deserialized document - has to be bundle identifier
        // local part
        .map(document -> document
            .withIdentifier(document.getCpmDocument().map(cpm -> cpm.getBundleId().getLocalPart()).orElse(null)))
        // validate bundle identifier namespace uri.
        .delayUntil(document -> Mono.justOrEmpty(document.getCpmDocument())
            .map(CpmDocument::getBundleId)
            .flatMap(MONO.makeSure(
                qn -> Objects.nonNull(qn.getNamespaceURI()),
                uri -> new BadRequestException(
                    "The bundle namespace uri '" + uri + "' does not resolve into known storage!!")))
            .map(QualifiedName::getNamespaceURI)
            .flatMap(MONO.makeSure(
                uri -> uri.equals(this.configuration.getFqdn() + "documents/"),
                uri -> new BadRequestException(
                    "The bundle namespace uri '" + uri + "' does not resolve into known storage!!"))))
        .flatMap(this::checkDocumentDoesNotExists)
        .delayUntil(this::checkBackwardConnetorsAttrs)
        .flatMap(this::checkForwardConnetorsAttrs)
        .delayUntil(this::checkBackwardConnectorResolvable)
        .delayUntil(this::checkForwardConnetorsResolvable)

        // TODO: check hashes in connectors
        // TODO: check cpm constraints
        // TODO: check provenance constraints
        // issue token
        .flatMap((Document document) -> Mono.justOrEmpty(document)
            .map(Document::getOrganizationIdentifier)
            .flatMap(organizationIdentifier -> Mono.justOrEmpty(organizationIdentifier)
                .flatMap(this.trustedPartyService::getTrustedPartyUrlByOrganizationIdentifier)
                .map(Optional::ofNullable)
                .flatMap(optUrl -> this.trustedPartyWebService.issueGraphToken(optUrl).apply(document))
                .map((Token token) -> token.withDocument(document))
                .flatMap((Token token) -> this.trustedPartyService
                    .getTrustedPartyByOrganizationIdentifier(organizationIdentifier)
                    .map(token::withTrustedParty))
                .flatMap(tokenService::storeToken))
            .map(token -> document.withToken(token)))
        .delayUntil((Document document) -> Mono.just(document)
            .map(Document::getCpmDocument)
            .flatMap(Mono::justOrEmpty)
            .flatMap((CpmDocument cpm) -> Mono.just(cpm)
                .flatMap(CpmDocumentUtils.REACTIVE::getMainActivityReferenceMetaBundleId)
                .flatMap(this.metaComponentService::getMetaComponent)
                .flatMap(this.metaComponentService.addNewVersion(cpm.getBundleId()))
                .flatMap(meta -> Mono.justOrEmpty(document.getToken())
                    .flatMap(token -> this.metaComponentService.addTokenToLastVersion(token).apply(
                        meta)))))
        .delayUntil(this.organizationService::linkOwnedDocument)
        .map(Document::getToken)
        .flatMap(Mono::justOrEmpty)
        .flatMap(DTOFactory::toTokenDTO);
  }

  private String getReferenceValue(Element element, CpmAttribute attr) {
    return Optional.ofNullable(CpmUtilities.getCpmAttributeValue(element, attr))
        .flatMap((Object obj) -> {
          if (obj instanceof QualifiedName qn) {
            return Optional.ofNullable(qn);
          }
          return Optional.empty();
        })
        .map(qn -> qn.getNamespaceURI() + qn.getLocalPart())
        .orElse("???");
  }

  private Mono<Boolean> isResolvableBundleId(Element connector) {
    if (connector instanceof Entity entity) {
      return Mono.justOrEmpty(CpmUtilities.getCpmAttributeValue(entity, CpmAttribute.REFERENCED_BUNDLE_ID))
          .flatMap((Object value) -> {
            if (value instanceof QualifiedName qn) {
              return Mono.justOrEmpty(qn);
            }
            return Mono.error(new BadRequestException());
          })
          .flatMap(this.storeWebService::pingQualifiedName);
    }
    return Mono.error(new BadRequestException(
        "Invalid connector. Statement with id '" + connector.getId().toString() + "' is not entity!"));
  }

  private Mono<Boolean> isResolvableMetaBundleId(Element connector) {
    if (connector instanceof Entity entity) {
      return Mono.justOrEmpty(CpmUtilities.getCpmAttributeValue(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID))
          .flatMap((Object value) -> {
            if (value instanceof QualifiedName qn) {
              return Mono.justOrEmpty(qn);
            }
            return Mono.error(new BadRequestException());
          })
          .flatMap(this.storeWebService::pingQualifiedName);
    }
    return Mono.error(new BadRequestException(
        "Invalid connector. Statement with id '" + connector.getId().toString() + "' is not entity!"));
  }

  private Boolean isValidBackwardConnector(Element connector) {
    if (connector instanceof Entity entity) {
      return CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
          // TODO: referencedBundleSpecV
          // TODO: referencedMetaBundleSpecV
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
    }
    return false;
  }

  private Mono<Document> checkForwardConnetorsAttrs(Document document) {
    return Mono.justOrEmpty(document)
        .delayUntil(
            prov -> Mono.justOrEmpty(prov.getCpmDocument())
                .flatMapMany(cpm -> Flux.fromIterable(cpm.getForwardConnectors()))
                .map(INode::getAnyElement)
                .flatMap(MONO.makeSure(
                    element -> this.isSpecForwardConnector(element)
                        ? this.isValidSpecForwardConnector(element)
                        : this.isValidForwardConnector(element),
                    (Element element) -> new BadRequestException(
                        "Element '" + element.getId() + "' is not valid forward connector"))));
  }

  private Mono<Document> checkForwardConnetorsResolvable(Document document) {
    return Mono.justOrEmpty(document)
        .delayUntil(prov -> Mono.justOrEmpty(prov.getCpmDocument())
            .flatMapMany(cpm -> Flux.fromIterable(cpm.getForwardConnectors()))
            .map(INode::getAnyElement)
            .filter(this::isSpecForwardConnector)
            .flatMap(MONO.makeSureAsync(
                this::isResolvableBundleId,
                (Element element) -> new BadRequestException(
                    "Reference bundle id '"
                        + getReferenceValue(element, CpmAttribute.REFERENCED_BUNDLE_ID)
                        + "' is not resolvable. Element '" + element.getId()
                        + "' is not valid forward connector.")))
            .flatMap(MONO.makeSureAsync(
                this::isResolvableMetaBundleId,
                (Element element) -> new BadRequestException(
                    "Reference meta bundle id '"
                        + getReferenceValue(element, CpmAttribute.REFERENCED_META_BUNDLE_ID)
                        + "' is not resolvable. Element '" + element.getId()
                        + "' is not valid forward connector."))));
  }

  private Mono<Document> checkDocumentDoesNotExists(Document document) {
    return MONO.<Document>makeSureAsync(
        doc -> Mono.justOrEmpty(doc.getIdentifier())
            .flatMap(this.documentService::getDocumentByIdentifier)
            .thenReturn(false)
            .onErrorResume(NotFoundException.class, _ -> Mono.just(true)),
        doc -> new ConflictException("Document with identifier '" + doc.getIdentifier() + "' exists!!"))
        .apply(document);
  }

  private Mono<Document> checkBackwardConnetorsAttrs(Document document) {
    return Mono.justOrEmpty(document)
        .delayUntil(
            prov -> Mono.justOrEmpty(prov.getCpmDocument())
                .flatMapMany(cpm -> Flux.fromIterable(cpm.getBackwardConnectors()))
                .map(INode::getAnyElement)
                .flatMap(MONO.makeSure(
                    this::isValidBackwardConnector,
                    (Element element) -> new BadRequestException(
                        "Element '" + element.getId() + "' is not valid backward connector"))));
  }

  private Mono<Document> checkBackwardConnectorResolvable(Document document) {
    return Mono.justOrEmpty(document)
        .delayUntil(prov -> Mono.justOrEmpty(prov.getCpmDocument())
            .flatMapMany(cpm -> Flux.fromIterable(cpm.getBackwardConnectors()))
            .map(INode::getAnyElement)
            .flatMap(MONO.makeSureAsync(
                this::isResolvableBundleId,
                (Element element) -> new BadRequestException(
                    "Reference bundle id '" + getReferenceValue(element,
                        CpmAttribute.REFERENCED_BUNDLE_ID)
                        + "' is not resolvable. Element '"
                        + element.getId()
                        + "' is not valid backward connector.")))
            .flatMap(MONO.makeSureAsync(
                this::isResolvableMetaBundleId,
                (Element element) -> new BadRequestException(
                    "Reference meta bundle id '"
                        + getReferenceValue(element, CpmAttribute.REFERENCED_META_BUNDLE_ID)
                        + "' is not resolvable. Element '" + element.getId()
                        + "' is not valid backward connector."))));
  }

  private Boolean isSpecForwardConnector(Element connector) {
    return connector.getOther().isEmpty()
        ? false
        : true;
  }

  private Boolean isValidForwardConnector(Element connector) {
    if (connector instanceof Entity entity) {
      return entity.getOther().isEmpty();
    }
    return false;
  }

  private Boolean isValidSpecForwardConnector(Element connector) {
    if (connector instanceof Entity entity) {
      return CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
          // TODO: referencedBundleSpecV
          // TODO: referencedMetaBundleSpecV
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
    }
    return false;
  }

  @NotNull
  @GetMapping("/{identifier}")
  @Operation(summary = "Get provenance document by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<DocumentResponseDTO> getFinalizedProvDocumentByIdentifier(@PathVariable String identifier) {
    return Mono.justOrEmpty(identifier)
        .flatMap(this.tokenService::getByDocumentIdentifier)
        .flatMap(DTOFactory::toDocumentDTO);
  }

  @NotNull
  @GetMapping("/{identifier}/domain-specific")
  @Operation(summary = "Get domain specific provenance document by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<DocumentResponseDTO> getDomainProvDocumentByIdentifier(@PathVariable String identifier) {
    return Mono.justOrEmpty(identifier)
        .flatMap(this.documentService::getDocumentByIdentifier)
        .map(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory))
        .flatMap(document -> Mono.justOrEmpty(document.getCpmDocument())
            .switchIfEmpty(Mono.error(new NotFoundException(
                "Finalized provenance document for identifier '" + identifier + "' can not be deserialized.")))
            .map(cpm -> new CpmDocument(
                cpm.getBundleId(),
                Collections.emptyList(),
                cpm.getDomainSpecificPart(),
                Collections.emptyList(),
                this.provFactory,
                this.cpmProvFactory,
                this.cpmFactory))
            .map(cpm -> ProvDocumentUtils.serialize(cpm.toDocument(), Formats.ProvFormat.JSON))
            .map(Base64Utils::encodeFromString)
            .map(cpmStr -> document
                .withGraph(cpmStr)
                .withCpmDocument(provFactory, cpmProvFactory, cpmFactory, true))
            .flatMap(provDoc -> Mono.justOrEmpty(provDoc.getIdentifier())
                .flatMap(this.documentService::getOrganizationIdentifierByIdentifier)
                .map(provDoc::withOrganizationIdentifier))
            .flatMap(provDoc -> Mono.justOrEmpty(provDoc)
                .map(Document::getOrganizationIdentifier)
                .flatMap(this.trustedPartyService::getTrustedPartyUrlByOrganizationIdentifier)
                .map(Optional::ofNullable)
                .flatMap(optUrl -> this.trustedPartyWebService.issueDomainSpecificGraphToken(optUrl).apply(provDoc))
                .map(token -> token.withDocument(provDoc)))
            .flatMap(token -> Mono.justOrEmpty(token.getDocument().getOrganizationIdentifier())
                .flatMap(this.trustedPartyService::getTrustedPartyByOrganizationIdentifier)
                .map(token::withTrustedParty)))
        .flatMap(DTOFactory::toDocumentDTO);
  }

  @NotNull
  @GetMapping("/{identifier}/backbone")
  @Operation(summary = "Get backbone provenance document by identifier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document fetched"),
      @ApiResponse(responseCode = "404", description = "Document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<DocumentResponseDTO> getBackboneProvDocumentByIdentifier(@PathVariable String identifier) {
    return Mono.justOrEmpty(identifier)
        .flatMap(this.documentService::getDocumentByIdentifier)
        .map(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory))
        .flatMap(document -> Mono.justOrEmpty(document.getCpmDocument())
            .switchIfEmpty(Mono.error(new NotFoundException(
                "Finalized provenance document for identifier '" + identifier + "' can not be deserialized.")))
            .map(cpm -> new CpmDocument(
                cpm.getBundleId(),
                cpm.getTraversalInformationPart(),
                Collections.emptyList(),
                Collections.emptyList(),
                this.provFactory,
                this.cpmProvFactory,
                this.cpmFactory))
            .map(cpm -> ProvDocumentUtils.serialize(cpm.toDocument(), Formats.ProvFormat.JSON))
            .map(Base64Utils::encodeFromString)
            .map(cpmStr -> document
                .withGraph(cpmStr)
                .withCpmDocument(provFactory, cpmProvFactory, cpmFactory, true))
            .flatMap(provDoc -> Mono.justOrEmpty(provDoc.getIdentifier())
                .flatMap(this.documentService::getOrganizationIdentifierByIdentifier)
                .map(provDoc::withOrganizationIdentifier))
            .flatMap(provDoc -> Mono.justOrEmpty(provDoc)
                .map(Document::getOrganizationIdentifier)
                .flatMap(this.trustedPartyService::getTrustedPartyUrlByOrganizationIdentifier)
                .map(Optional::ofNullable)
                .flatMap(optUrl -> this.trustedPartyWebService.issueDomainSpecificGraphToken(optUrl).apply(provDoc))
                .map(token -> token.withDocument(provDoc)))
            .flatMap(token -> Mono.justOrEmpty(token.getDocument().getOrganizationIdentifier())
                .flatMap(this.trustedPartyService::getTrustedPartyByOrganizationIdentifier)
                .map(token::withTrustedParty)))
        .flatMap(DTOFactory::toDocumentDTO);
  }

  @Override
  @NotNull
  @RequestMapping(path = "/{identifier}", method = RequestMethod.HEAD)
  @Operation(summary = "Check if a document exists")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Document exists"),
      @ApiResponse(responseCode = "404", description = "Document does not exist"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Mono<Void> exists(@PathVariable String identifier) {
    return Mono.justOrEmpty(identifier)
        .flatMap(MONO.makeSureAsync(
            this.documentService::existsByIdentifier,
            id -> new NotFoundException("Document with identifier '" + id + "' does not exist!")))
        .then();
  }
}
