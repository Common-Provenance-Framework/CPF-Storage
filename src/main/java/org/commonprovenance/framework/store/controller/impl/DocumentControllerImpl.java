package org.commonprovenance.framework.store.controller.impl;

import org.commonprovenance.framework.store.controller.DocumentController;
import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.persistence.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/documents")
public class DocumentControllerImpl implements DocumentController {
  private final DocumentService service;

  public DocumentControllerImpl(DocumentService service) {
    this.service = service;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping()
  @NotNull
  public Mono<DocumentResponseDTO> createProvDocument(
      @RequestBody DocumentFormDTO body) {
    return ModelFactory.toDomain(body)
        .flatMap(this.service::storeDocument)
        .flatMap(DTOFactory::toDTO);
  }

  @GetMapping()
  @NotNull
  public Flux<DocumentResponseDTO> getAllProvDocuments() {
    return this.service.getAllDocuments()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<DocumentResponseDTO> getProvDocumentById(@PathVariable String uuid) {
    return ModelFactory.toUUID(uuid)
        .flatMap(this.service::getDocumentById)
        .flatMap(DTOFactory::toDTO);
  }
}
