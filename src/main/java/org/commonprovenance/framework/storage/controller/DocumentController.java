package org.commonprovenance.framework.storage.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.commonprovenance.framework.storage.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.storage.controller.mapper.DTOMapper;
import org.commonprovenance.framework.storage.controller.mapper.DomainMapper;
import org.commonprovenance.framework.storage.controller.validator.IsUUID;
import org.commonprovenance.framework.storage.service.impl.DocumentServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.commonprovenance.framework.storage.controller.dto.form.DocumentFormDTO;

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
    return DomainMapper.toDomain(body)
        .flatMap(this.documentService::storeDocument)
        .flatMap(DTOMapper::toDTO);
  }

  @GetMapping()
  @NotNull
  public Flux<DocumentResponseDTO> getAllProvDocuments() {
    return this.documentService.getAllDocuments()
        .flatMap(DTOMapper::toDTO);
  }

  @NotNull
  @GetMapping("/{uuid}")
  public Mono<DocumentResponseDTO> getProvDocumentById(@PathVariable @IsUUID String uuid) {
    return DomainMapper.toDomain(uuid)
      .flatMap(this.documentService::getDocumentById)
      .flatMap(DTOMapper::toDTO);
  }
}
