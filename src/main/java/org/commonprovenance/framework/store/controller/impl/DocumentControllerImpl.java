package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.AdditionalData;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.springframework.http.HttpStatus;
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
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.model.CpmDocument;
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
  private final TokenService tokenService;
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
    this.trustedPartyWebService = trustedPartyWebService;
    this.storeWebService = storeWebService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;

    this.configuration = configuration;
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
        // validate bundle identifier namespace uri.
        .delayUntil(document -> Mono.justOrEmpty(document.getCpmDocument())
            .map(cpm -> cpm.getBundleId().getNamespaceURI())
            .flatMap(MONO.makeSure(uri -> uri.equals(this.configuration.getFqdn() + "documents/"),
                _ -> new BadRequestException("The bundle identifier does not resolve into document: "))))
        .flatMap(this::checkDocumentDoesNotExists)
        .delayUntil(this::checkBackwardConnetorsAttrs)
        .flatMap(this::checkForwardConnetorsAttrs)
        .delayUntil(this::checkBackwardConnectorResolvable)
        .delayUntil(this::checkForwardConnetorsResolvable)

        // TODO: check hashes in connectors
        // TODO: check cpm constraints
        // TODO: check provenance constraints
        // issue token
        .flatMap(
            (Document document) -> this.organizationService.getOrganizationById(document.getOrganizationId())
                .flatMap((Organization org) -> Mono.just(getTrustedPartyUrl(org))
                    .map(this.trustedPartyWebService::issueGraphToken)
                    // Store Document with token
                    .flatMap((Function<Document, Mono<Token>> issueToken) -> issueToken
                        .apply(document.withOrganizationName(org.getName())))
                    .map((Token token) -> token.withDocument(document))
                    .map((Token token) -> token.withTrustedParty(org.getTrustedParty())))
                .flatMap(tokenService::storeToken)
                .map(token -> document.withToken(token)))
        .delayUntil((Document document) -> Mono.just(document)
            .map(Document::getCpmDocument)
            .flatMap(Mono::justOrEmpty)
            .flatMap((CpmDocument cpm) -> Mono.just(cpm)
                .flatMap(this::getReferenceMetaBundleId)
                .flatMap(this.metaComponentService::getMetaComponent)
                .flatMap(this.metaComponentService.addNewVersion(cpm.getBundleId()))
                .flatMap(meta -> Mono.justOrEmpty(document.getToken())
                    .flatMap(token -> this.metaComponentService.addTokenToLastVersion(token).apply(meta)))))
        .flatMap(DTOFactory::toDTO);
  }

  private Optional<String> getTrustedPartyUrl(Organization organization) {
    return Optional.ofNullable(organization)
        .map(Organization::getTrustedParty)
        .flatMap(TrustedParty::getUrl);
  }

  // private Mono<org.openprovenance.prov.model.Document>
  // buildMetaComponent(Document document) {

  // org.openprovenance.prov.model.Document provDocument =
  // this.provFactory.newDocument();
  // provDocument.getNamespace().addKnownNamespaces();
  // provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX,
  // CpmNamespaceConstants.CPM_NS);
  // provDocument.getNamespace().register("pav", "http://purl.org/pav/");
  // provDocument.getNamespace().register("meta", this.configuration.getFqdn() +
  // "documents/meta/");
  // provDocument.getNamespace().register("storage", this.configuration.getFqdn()
  // + "documents/");

  // Mono<QualifiedName> bundleId = Mono.justOrEmpty(document.getCpmDocument())
  // .flatMap(this::getReferenceMetaBundleId);

  // Mono<QualifiedName> identifier =
  // Mono.justOrEmpty(document.getCpmDocument()).map(CpmDocument::getBundleId);
  // Map<String, String> namespaces = provDocument.getNamespace().getNamespaces();

  // Mono<Entity> generalEntity = identifier
  // .map(i -> provFactory.newEntity(provFactory.newQualifiedName(
  // i.getNamespaceURI(),
  // UUID.randomUUID().toString(),
  // i.getPrefix())))
  // .doOnNext(general -> general.getType().add(provFactory.newType(
  // provFactory.getName().PROV_BUNDLE,
  // provFactory.getName().PROV_TYPE)));

  // Mono<Entity> firstVersion = identifier
  // .map(provFactory::newEntity)
  // .doOnNext(first -> {
  // first.getType().add(provFactory.newType(
  // provFactory.getName().PROV_BUNDLE,
  // provFactory.getName().PROV_TYPE));

  // first.getOther().add(provFactory.newOther(
  // provFactory.newQualifiedName(namespaces.get("pav"), "version", "pav"),
  // 1,
  // provFactory.getName().XSD_INT));
  // });

  // Mono<Entity> token = identifier
  // .map(i -> provFactory.newEntity(provFactory.newQualifiedName(
  // i.getNamespaceURI(),
  // UUID.randomUUID().toString(),
  // i.getPrefix())))
  // .doOnNext(t -> {
  // t.getType().add(provFactory.newType(
  // cpmProvFactory.newCpmQualifiedName("token"),
  // provFactory.getName().PROV_TYPE));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("originatorId"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getOriginatorName).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("authorityId"),
  // document.getToken().map(Token::getTrustedParty).map(TrustedParty::getName).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("tokenTimestamp"),
  // document.getToken().map(Token::getCreatedOn).get(),
  // provFactory.getName().XSD_LONG));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("documentCreationTimestamp"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getDocumentTimestamp).get(),
  // provFactory.getName().XSD_LONG));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("documentDigest"),
  // document.getToken().map(Token::getHash).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("bundle"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getBundle).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("hashFunction"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getHashFunction).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("trustedPartyUri"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getTrustedPartyUri).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("trustedPartyCertificate"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getTrustedPartyCertificate).get(),
  // provFactory.getName().XSD_STRING));

  // t.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("signature"),
  // document.getToken().map(Token::getSignature).get(),
  // provFactory.getName().XSD_STRING));
  // });

  // Mono<Agent> agent = Mono.justOrEmpty(document.getToken()
  // .map(Token::getTrustedParty)
  // .map(TrustedParty::getId))
  // .flatMap(Mono::justOrEmpty)
  // .map(uuid -> provFactory.newAgent(provFactory.newQualifiedName(
  // namespaces.get("storage"),
  // uuid.toString(),
  // "storage"))) // TODO: ??identifier ns??
  // .doOnNext(a -> {
  // a.getType().add(provFactory.newType(
  // cpmProvFactory.newCpmQualifiedName("trustedParty"),
  // provFactory.getName().PROV_TYPE));

  // a.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("trustedPartyUri"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getTrustedPartyUri).get(),
  // provFactory.getName().XSD_STRING));

  // a.getOther().add(provFactory.newOther(
  // cpmProvFactory.newCpmQualifiedName("trustedPartyCertificate"),
  // document.getToken().map(Token::getAdditionalData).map(AdditionalData::getTrustedPartyCertificate).get(),
  // provFactory.getName().XSD_STRING));
  // });

  // Mono<Activity> activity = identifier
  // .map(i -> provFactory.newActivity(provFactory.newQualifiedName(
  // i.getNamespaceURI(),
  // UUID.randomUUID().toString(),
  // i.getPrefix())))
  // .doOnNext(act -> {

  // try {
  // GregorianCalendar tokenTimestamp = new GregorianCalendar();
  // tokenTimestamp.setTimeInMillis(document.getToken().map(Token::getCreatedOn).get());
  // XMLGregorianCalendar timestampVal =
  // DatatypeFactory.newInstance().newXMLGregorianCalendar(tokenTimestamp);
  // act.setStartTime(timestampVal);
  // act.setEndTime(timestampVal);

  // } catch (DatatypeConfigurationException e) {

  // }
  // act.getType().add(provFactory.newType(
  // cpmProvFactory.newCpmQualifiedName("tokenGeneration"),
  // provFactory.getName().PROV_TYPE));
  // });
  // Namespace bundleNs = provFactory.newNamespace();
  // bundleNs.register("meta", this.configuration.getFqdn() + "documents/meta/");

  // return bundleId.flatMap(
  // id -> Mono.zip(generalEntity, firstVersion, token, agent, activity)
  // .map(tuple -> {
  // List<Statement> statements = new ArrayList<>();
  // statements.add(tuple.getT1());
  // statements.add(tuple.getT2());
  // statements.add(tuple.getT3());
  // statements.add(tuple.getT4());
  // statements.add(tuple.getT5());
  // statements.add(provFactory.newSpecializationOf(tuple.getT2().getId(),
  // tuple.getT1().getId()));
  // statements.add(provFactory.newUsed(tuple.getT5().getId(),
  // tuple.getT2().getId()));
  // statements.add(provFactory.newWasAssociatedWith(null, tuple.getT5().getId(),
  // tuple.getT4().getId()));
  // statements.add(provFactory.newWasGeneratedBy(null, tuple.getT3().getId(),
  // tuple.getT5().getId()));
  // statements.add(provFactory.newWasAttributedTo(null, tuple.getT3().getId(),
  // tuple.getT4().getId()));
  // return statements;
  // })
  // .map(statements -> {
  // Bundle bundle = provFactory.newNamedBundle(id, bundleNs, statements);
  // provDocument.getStatementOrBundle().add(bundle);
  // return provDocument;
  // }));
  // }

  private Mono<QualifiedName> getReferenceMetaBundleId(CpmDocument cpm) {
    return Mono.justOrEmpty(cpm)
        .map(CpmDocument::getMainActivity)
        .map(INode::getAnyElement)
        .map(Element::getOther)
        .flatMapMany(Flux::fromIterable)
        .filter((Other other) -> other.getElementName().getLocalPart()
            .equals(CpmAttribute.REFERENCED_META_BUNDLE_ID.toString()))
        .single()
        .map(Other::getValue)
        .flatMap(value -> (value instanceof QualifiedName qn)
            ? Mono.just(qn)
            : Mono.error(new InvalidValueException("Attribute '" + CpmAttribute.REFERENCED_META_BUNDLE_ID
                .toString() + "' should has type QualifiedName!")));

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
        doc -> Mono.justOrEmpty(doc.getId())
            .flatMap(this.documentService::getDocumentById)
            .thenReturn(false)
            .onErrorResume(NotFoundException.class, _ -> Mono.just(true)),
        doc -> new ConflictException("Document with id '" + doc.getId() + "' exists!!"))
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

  @GetMapping()
  @NotNull
  public Flux<DocumentResponseDTO> getAllProvDocuments() {
    return this.documentService.getAllDocuments()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<DocumentResponseDTO> getProvDocumentById(@PathVariable String uuid) {
    return Mono.justOrEmpty(uuid)
        .flatMap(this.documentService::getDocumentById)
        .flatMap(DTOFactory::toDTO);
  }

  @Override
  @NotNull
  @RequestMapping(path = "/{uuid}", method = RequestMethod.HEAD)
  public Mono<Void> exists(@PathVariable String uuid) {
    return Mono.justOrEmpty(uuid).log()
        .flatMap(this.documentService::getDocumentById)
        .then();
  }
}
