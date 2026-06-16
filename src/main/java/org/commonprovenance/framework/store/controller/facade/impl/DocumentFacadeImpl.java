package org.commonprovenance.framework.store.controller.facade.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Collections;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DocumentResponseFactory;
import org.commonprovenance.framework.store.controller.dto.response.factory.TokenResponseFactory;
import org.commonprovenance.framework.store.controller.facade.DocumentFacade;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.model.utils.DocumentUtils;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaProvenanceComponentService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@Component
public class DocumentFacadeImpl implements DocumentFacade {
  private final String LOG_PREFIX = "DocumentFacade: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentFacadeImpl.class);

  private final DocumentService documentService;
  private final OrganizationService organizationService;
  private final TrustedPartyService trustedPartyService;

  private final MetaProvenanceComponentService metaComponentService;

  private final TrustedPartyWebService trustedPartyWebService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  private final AppConfiguration configuration;

  public DocumentFacadeImpl(
      DocumentService documentService,
      OrganizationService organizationService,
      TrustedPartyService trustedPartyService,
      TrustedPartyWebService trustedPartyWebService,
      MetaProvenanceComponentService metaComponentService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory,
      AppConfiguration configuration) {
    this.documentService = documentService;
    this.organizationService = organizationService;
    this.trustedPartyService = trustedPartyService;

    this.metaComponentService = metaComponentService;
    this.trustedPartyWebService = trustedPartyWebService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;

    this.configuration = configuration;

  }

  @Override
  public Mono<TokenResponseDTO> createProvDocument(Organization organization, DocumentFormDTO body) {
    return Mono.just(organization)
        .flatMap(MONO.liftEffectToMono(org -> Either.<ApplicationException, DocumentFormDTO> right(body)
            .map(DocumentFactory::build)
            .flatMap(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory))
            .map(org::withDocument)))
        .doOnNext(_ -> LOGGER.debug("{} Document has been deserialized and loaded.", LOG_PREFIX))
        .delayUntil(this.trustedPartyWebService.verifySignature(body.getSignature()))
        .doOnNext(_ -> LOGGER.debug("{} Signature has been verified.", LOG_PREFIX))
        .delayUntil(MONO.liftEffectToMono(DocumentUtils.checkBundleId(this.configuration)))
        .delayUntil(org -> Mono.just(org)
            .flatMap(MONO.liftOptionalToMono(
                Organization::getDocument,
                _ -> new InvalidValueException("Document has not been deserialized yet!")))
            // check document does not exists yet
            .delayUntil(this.documentService::checkDocumentDoesNotExists)
            // check connectors
            .delayUntil(this.documentService::checkBackwardConnectorsResolvable)
            .delayUntil(this.documentService::checkSpecForwardConnectorsResolvable)
            .delayUntil(MONO.liftEffectToMono(DocumentUtils::checkSpecForwardConnetorsAttrs))
            .delayUntil(MONO.liftEffectToMono(DocumentUtils::checkBackwardConnetorsAttrs))
            .delayUntil(MONO.liftEffectToMono(DocumentUtils::checkForwardConnetorsAttrs))
        // TODO: check hashes in connectors
        // TODO: check cpm constraints
        // TODO: check provenance constraints
        )
        .doOnNext(_ -> LOGGER.debug("Document has been validated and considered as valid."))
        .flatMap(this.trustedPartyWebService.issueGraphToken(body.getSignature()))
        .doOnNext(_ -> LOGGER.debug("Token has been issued by TrustedParty."))
        .delayUntil(this.organizationService::storeDocument)
        .doOnNext(_ -> LOGGER.debug("Document has been saved."))
        .delayUntil(this.metaComponentService::createMetaProvenanceComponentIfNotExists)
        .delayUntil(this.metaComponentService::addBundleVersionIntoMetaProvenanceComponent)
        .delayUntil(this.metaComponentService::addTokenIntoMetaProvenanceComponent)

        .doOnNext(_ -> LOGGER.debug("MetaComponent stored"))
        .flatMap(MONO.liftEffectToMono(TokenResponseFactory::build))
        .doOnNext(_ -> LOGGER.debug("Finito.."));
  }

  @Override
  public Mono<DocumentResponseDTO> getProvDocument(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(Organization::getDocument))
        .map(DocumentResponseFactory::build);
  }

  @Override
  public Mono<DocumentResponseDTO> getDomainProvDocument(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(Organization::getDocument))
        .flatMap(MONO.liftOptionalToMono(Document::getCpmDocument))
        .map(cpm -> new CpmDocument(
            cpm.getBundleId(),
            Collections.emptyList(),
            cpm.getDomainSpecificPart(),
            Collections.emptyList(),
            this.provFactory,
            this.cpmProvFactory,
            this.cpmFactory))
        .flatMap(MONO.liftEffectToMono(DocumentUtils.serialize(Formats.ProvFormat.JSON)))
        .flatMap(MONO.liftEffectToMono(Base64Utils::encodeFromString))
        .flatMap(MONO.liftEffectToMono(cpmStr -> new Document(cpmStr, Format.JSON)
            .withCpmDocument(provFactory, cpmProvFactory, cpmFactory)))
        .map(organization::withDocument)
        .flatMap(this.trustedPartyWebService::issueDomainSpecificGraphToken)
        .flatMap(MONO.liftOptionalToMono(Organization::getDocument))
        .map(DocumentResponseFactory::build);

  }

  @Override
  public Mono<DocumentResponseDTO> getBackboneProvDocument(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(Organization::getDocument))
        .flatMap(MONO.liftOptionalToMono(Document::getCpmDocument))
        .map(cpm -> new CpmDocument(
            cpm.getBundleId(),
            cpm.getTraversalInformationPart(),
            Collections.emptyList(),
            Collections.emptyList(),
            this.provFactory,
            this.cpmProvFactory,
            this.cpmFactory))
        .flatMap(MONO.liftEffectToMono(DocumentUtils.serialize(Formats.ProvFormat.JSON)))
        .flatMap(MONO.liftEffectToMono(Base64Utils::encodeFromString))
        .flatMap(MONO.liftEffectToMono(cpmStr -> new Document(cpmStr, Format.JSON)
            .withCpmDocument(provFactory, cpmProvFactory, cpmFactory)))
        .map(organization::withDocument)
        .flatMap(this.trustedPartyWebService::issueBackboneGraphToken)
        .flatMap(MONO.liftOptionalToMono(Organization::getDocument))
        .map(DocumentResponseFactory::build);
  }

}
