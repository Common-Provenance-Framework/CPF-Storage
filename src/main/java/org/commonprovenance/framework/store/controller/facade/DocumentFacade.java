package org.commonprovenance.framework.store.controller.facade;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface DocumentFacade {
  Mono<TokenResponseDTO> createProvDocument(Organization organization, DocumentFormDTO body);

  Mono<DocumentResponseDTO> getProvDocument(Organization organization);

  Mono<DocumentResponseDTO> getDomainProvDocument(Organization organization);

  Mono<DocumentResponseDTO> getBackboneProvDocument(Organization organization);

}
