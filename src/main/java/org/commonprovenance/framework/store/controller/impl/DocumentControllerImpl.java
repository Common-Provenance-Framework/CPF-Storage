package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.DocumentService;
import org.commonprovenance.framework.store.service.persistence.OrganizationService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.ProvFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.model.INode;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/documents")
public class DocumentControllerImpl implements DocumentController {
  private final DocumentService documentService;
  private final OrganizationService organizationService;
  private final TrustedPartyWebService trustedPartyWebService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  public DocumentControllerImpl(
      DocumentService documentService,
      OrganizationService organizationService,
      TrustedPartyWebService trustedPartyWebService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory) {
    this.documentService = documentService;
    this.organizationService = organizationService;
    this.trustedPartyWebService = trustedPartyWebService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping()
  @NotNull
  public Mono<DocumentResponseDTO> createProvDocument(
      @RequestBody DocumentFormDTO body) {
    return ModelFactory.toDomain(body)
        // validate Organization and TrustedParty first
        .delayUntil((Document document) -> Mono.just(new Organization())
            .map(organization -> organization.withId(document.getOrganizationId()))
            .flatMap(MONO.makeSureAsync(
                this.organizationService::exists,
                org -> new ConflictException("Organization with id " + org.getId().map(UUID::toString).orElse("")
                    + " has not been registered yet!")))
            .flatMap(organization -> Mono.justOrEmpty(organization.getId())
                .flatMap(this.organizationService::getOrganizationById))
            .map(organization -> organization.getTrustedParty())
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
        .delayUntil((Document document) -> Mono.justOrEmpty(document.getOrganizationId())
            .flatMap(this.organizationService::getOrganizationById)
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
            .withId(document.getCpmDocument().map(cpm -> cpm.getBundleId().getLocalPart()).map(UUID::fromString)
                .orElse(null)))
        // validate bundle identifier namespace uri. But has to be validate in different
        // way!!
        .delayUntil(document -> Mono.justOrEmpty(document.getCpmDocument()
            .map(cpm -> cpm.getBundleId().getNamespaceURI().endsWith(
                "/documents/")))
            .flatMap(x -> x ? Mono.just(document)
                : Mono.error(new BadRequestException(
                    "The bundle identifier does not resolve into document: "))))
        // validate document doest not exists yet
        .flatMap(MONO.makeSureAsync(
            // do not exists
            doc -> this.documentService.getDocumentById(doc.getId().get()).thenReturn(false)
                .onErrorResume(NotFoundException.class, _ -> Mono.just(true)),
            doc -> new ConflictException("Document with id '" + doc.getId().map(UUID::toString).get() + "' exists!!")))

        // validate backward connector attributes
        .delayUntil(document -> Mono.justOrEmpty(document.getCpmDocument())
            .flatMapMany(cpm -> Flux.fromIterable(cpm.getBackwardConnectors()))
            .map(INode::getAnyElement)
            .flatMap(MONO.makeSure(
                this::isValidBackwardConnector,
                (Element element) -> new BadRequestException(
                    "Element '" + element.getId() + "' is not valid backward connector"))))
        // validate forward connector attributes
        .delayUntil(document -> Mono.justOrEmpty(document.getCpmDocument())
            .flatMapMany(cpm -> Flux.fromIterable(cpm.getForwardConnectors()))
            .map(INode::getAnyElement)
            .flatMap(MONO.makeSure(
                this::isValidForwardConnector,
                (Element element) -> new BadRequestException(
                    "Element '" + element.getId() + "' is not valid forward connector"))))
        .flatMap(DTOFactory::toDTO);
  }

  private Boolean isValidBackwardConnector(Element connector) {
    if (connector instanceof Entity entity) {
      return CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
          && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
    }
    return false;
  }

  private Boolean isValidForwardConnector(Element connector) {
    return CpmUtilities.containsCpmAttribute(connector, CpmAttribute.REFERENCED_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(connector, CpmAttribute.REFERENCED_META_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(connector, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
        // && CpmUtilities.containsCpmAttribute(connector,
        // CpmAttribute.PROVENANCE_SERVICE_URI) // is optional
        && CpmUtilities.containsCpmAttribute(connector, CpmAttribute.HASH_ALG);
  }

  @GetMapping()
  @NotNull
  public Flux<DocumentResponseDTO> getAllProvDocuments() {
    return this.documentService.getAllDocuments()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<DocumentResponseDTO> getProvDocumentById(@PathVariable String uuid) {
    return ModelFactory.toUUID(uuid)
        .flatMap(this.documentService::getDocumentById)
        .flatMap(DTOFactory::toDTO);
  }
}
