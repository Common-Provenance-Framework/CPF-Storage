package org.commonprovenance.framework.store.controller.impl;

import org.commonprovenance.framework.store.controller.MetaDocumentController;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.facade.MetaDocumentFacade;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadMetaDocument;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Validated
@RestController()
@RequestMapping("/api/v1/documents/meta")
@Tag(name = "Meta Documents", description = "Meta component endpoints")
public class MetaDocumentControllerImpl implements MetaDocumentController {
  private final MetaDocumentFacade metaDocumentFacade;

  public MetaDocumentControllerImpl(
      MetaDocumentFacade metaDocumentFacade) {
    this.metaDocumentFacade = metaDocumentFacade;
  }

  @Override
  @NotNull
  @RequestMapping(path = "/{uuid}", method = RequestMethod.HEAD)
  @Operation(summary = "Check if a meta document exists")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Meta document exists"),
      @ApiResponse(responseCode = "404", description = "Meta document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<Void> exists(@PathVariable String uuid) {
    return Mono.justOrEmpty(uuid)
        .flatMap(this.metaDocumentFacade::exists);
  }

  @Override
  @NotNull
  @RequestMapping(path = "/{identifier}", method = RequestMethod.GET)
  @Operation(summary = "Get meta document if exists", parameters = {
      @Parameter(name = "identifier", description = "Meta Document identifier", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string"))
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Meta document"),
      @ApiResponse(responseCode = "404", description = "Meta document not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotFoundDTO.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InternalServerErrorDTO.class)))
  })
  public Mono<DocumentResponseDTO> getMetaDocument(@Parameter(hidden = true) @LoadMetaDocument() MetaDocument metaDocument) {
    return Mono.justOrEmpty(metaDocument)
        .flatMap(this.metaDocumentFacade::getMetaDocument);

  }
}
