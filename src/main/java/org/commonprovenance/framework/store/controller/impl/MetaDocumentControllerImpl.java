package org.commonprovenance.framework.store.controller.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.controller.MetaDocumentController;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/documents/meta")
public class MetaDocumentControllerImpl implements MetaDocumentController {
  private final MetaComponentService metaComponentService;

  public MetaDocumentControllerImpl(
      MetaComponentService metaComponentService) {
    this.metaComponentService = metaComponentService;
  }

  @Override
  @NotNull
  @RequestMapping(path = "/{uuid}", method = RequestMethod.HEAD)
  public Mono<Void> exists(@PathVariable String uuid) {
    return Mono.justOrEmpty(uuid)
        .flatMap(MONO.makeSureAsync(
            this.metaComponentService::exists,
            id -> new NotFoundException("Meta Component with id '" + id + " does not exists! ")))
        .then();
  }
}
