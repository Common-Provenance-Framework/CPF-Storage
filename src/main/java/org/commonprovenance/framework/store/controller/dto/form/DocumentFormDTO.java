package org.commonprovenance.framework.store.controller.dto.form;

import org.commonprovenance.framework.store.common.dto.HasDocumentFormat;
import org.commonprovenance.framework.store.common.dto.HasOrganizationIdentifier;
import org.commonprovenance.framework.store.controller.validator.IsBase64String;
import org.commonprovenance.framework.store.controller.validator.IsJsonBase64;
import org.commonprovenance.framework.store.controller.validator.IsProvBase64Json;
import org.commonprovenance.framework.store.controller.validator.IsValueOfEnum;
import org.commonprovenance.framework.store.model.Format;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DocumentForm", description = "Payload used to create a provenance document")
public class DocumentFormDTO implements HasDocumentFormat, HasOrganizationIdentifier<DocumentFormDTO> {

  @Schema(description = "Organization identifier owning the document", example = "ORG1", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "OrganizationIdentifier should not be null or empty.")
  private final String organizationIdentifier;

  @Schema(description = "Base64 encoded PROV JSON document", example = "eyJwcm92On...", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Document should not be null or empty.")
  @IsBase64String(message = "Document should be Base64 string.")
  @IsJsonBase64(message = "Document should be Base64 json string")
  @IsProvBase64Json(message = "Document should be a Base64 provenance json string")
  private final String document;

  @Schema(description = "Input document format (case-insensitive, mapped to domain enum Format)", implementation = Format.class, allowableValues = {
      "JSON" }, example = "JSON", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Format should not be null or empty.")
  @IsValueOfEnum(enumClass = Format.class, message = "Invalid format.")
  private final String documentFormat;

  @Schema(description = "Digital signature for the encoded document", example = "MEQCIDv...", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Signature should not be null or empty.")
  private final String signature;

  @Schema(description = "Unix timestamp (seconds) when the document was created", example = "1719667200", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "CreatedOn should not be null.")
  private final Long createdOn;

  public DocumentFormDTO(
      String organizationIdentifier,
      String document,
      String documentFormat,
      String signature,
      Long createdOn) {
    this.organizationIdentifier = organizationIdentifier;
    this.document = document;
    this.documentFormat = documentFormat;
    this.signature = signature;
    this.createdOn = createdOn;
  }

  public DocumentFormDTO withOrganizationIdentifier(String organizationIdentifier) {
    return new DocumentFormDTO(
        organizationIdentifier,
        this.getDocument(),
        this.getDocumentFormat(),
        this.getSignature(),
        this.getCreatedOn());
  }

  public String getOrganizationIdentifier() {
    return organizationIdentifier;
  }

  public String getDocument() {
    return document;
  }

  public String getDocumentFormat() {
    return documentFormat;
  }

  public String getSignature() {
    return signature;
  }

  public Long getCreatedOn() {
    return createdOn;
  }

}
