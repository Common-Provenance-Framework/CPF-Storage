package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.factory.DTOFactory;
import org.commonprovenance.framework.store.controller.validator.IsUUID;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.service.impl.DocumentServiceImpl;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/documents")
public class DocumentController {
  private final DocumentServiceImpl documentService;

  public DocumentController(DocumentServiceImpl documentService) {
    this.documentService = documentService;
  }

  @PostMapping()
  @NotNull
  public Mono<DocumentResponseDTO> createProvDocument(@Valid @RequestBody @NotNull DocumentFormDTO body) {
    return ModelFactory.toDomain(body)
        .flatMap(this.documentService::storeDocument)
        .flatMap(DTOFactory::toDTO);
  }

  @GetMapping()
  @NotNull
  public Flux<DocumentResponseDTO> getAllProvDocuments() {
    return this.documentService.getAllDocuments()
        .flatMap(DTOFactory::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<DocumentResponseDTO> getProvDocumentById(@PathVariable @IsUUID String uuid) {
    return ModelFactory.toUUID(uuid)
        .flatMap(this.documentService::getDocumentById)
        .flatMap(DTOFactory::toDTO);
  }
}
