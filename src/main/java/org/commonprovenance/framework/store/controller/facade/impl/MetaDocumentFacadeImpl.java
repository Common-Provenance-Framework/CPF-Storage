package org.commonprovenance.framework.store.controller.facade.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DocumentResponseFactory;
import org.commonprovenance.framework.store.controller.facade.MetaDocumentFacade;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.service.persistence.MetaProvenanceComponentService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class MetaDocumentFacadeImpl implements MetaDocumentFacade {
  private final MetaProvenanceComponentService metaComponentService;
  private final TrustedPartyWebService trustedPartyWebService;

  public MetaDocumentFacadeImpl(
      MetaProvenanceComponentService metaComponentService,
      TrustedPartyWebService trustedPartyWebService) {
    this.metaComponentService = metaComponentService;
    this.trustedPartyWebService = trustedPartyWebService;
  }

  @Override
  public Mono<Void> exists(String identifier) {
    return Mono.justOrEmpty(identifier)
        .flatMap(MONO.makeSureAsync(
            this.metaComponentService::metaProvenanceComponentExists,
            id -> new NotFoundException("Meta Component with id '" + id + " does not exists! ")))
        .then();
  }

  @Override
  public Mono<DocumentResponseDTO> getMetaDocument(MetaDocument metaDocument) {
    return Mono.justOrEmpty(metaDocument)
        .flatMap(this.trustedPartyWebService::issueMetaToken)
        .flatMap(MONO.liftEffectToMono(DocumentResponseFactory::buildSafe));
  }
}
