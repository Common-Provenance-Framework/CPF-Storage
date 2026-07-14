package org.commonprovenance.framework.store.controller.dto.form;

import org.commonprovenance.framework.store.common.dtos.HasGraph;
import org.commonprovenance.framework.store.common.dtos.HasGraphFormat;
import org.commonprovenance.framework.store.common.dtos.HasSignature;
import org.commonprovenance.framework.store.controller.validator.IsBase64String;
import org.commonprovenance.framework.store.controller.validator.IsJsonBase64;
import org.commonprovenance.framework.store.controller.validator.IsProvBase64Json;
import org.commonprovenance.framework.store.model.GraphFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DocumentForm", description = "Payload used to create a provenance document")
public record DocumentFormDTO(

    @Schema(description = "Base64 encoded PROV JSON graph", example = "eyJwcm92On...", requiredMode = Schema.RequiredMode.REQUIRED)

    @NotBlank(message = "Graph should not be null or empty.")

    @IsBase64String(message = "Graph should be Base64 string.")

    @IsJsonBase64(message = "Graph should be Base64 json string")

    @IsProvBase64Json(message = "Graph should be a Base64 provenance json string")

    String graph,

    @Schema(description = "Input grpah format", implementation = GraphFormat.class, allowableValues = {
        "JSON" }, example = "JSON", requiredMode = Schema.RequiredMode.REQUIRED)

    @NotNull(message = "Format should not be null.")

    GraphFormat graphFormat,

    @Schema(description = "Digital signature for the encoded document", example = "MEQCIDv...", requiredMode = Schema.RequiredMode.REQUIRED)

    @NotBlank(message = "Signature should not be null or empty.")

    String signature) implements
    HasGraph,
    HasGraphFormat,
    HasSignature {

}
