package org.commonprovenance.framework.store.controller.dto.form;

import org.commonprovenance.framework.store.common.dto.HasDocumentFormat;
import org.commonprovenance.framework.store.controller.validator.IsBase64String;
import org.commonprovenance.framework.store.controller.validator.IsJsonBase64;
import org.commonprovenance.framework.store.controller.validator.IsProvBase64Json;
import org.commonprovenance.framework.store.controller.validator.IsValueOfEnum;
import org.commonprovenance.framework.store.model.Format;

import jakarta.validation.constraints.NotBlank;

public class DocumentFormDTO implements HasDocumentFormat {

  @NotBlank(message = "Document should not be null or empty.")
  @IsBase64String(message = "Document should be Base64 string.")
  @IsJsonBase64(message = "Document should be Base64 json string")
  @IsProvBase64Json(message = "Document should be a Base64 provenance json string")
  private final String document;

  @NotBlank(message = "Format should not be null or empty.")
  @IsValueOfEnum(enumClass = Format.class, message = "Invalid format.")
  private final String documentFormat;

  @NotBlank(message = "Signature should not be null or empty.")
  private final String signature;

  @NotBlank(message = "CreatedOn should not be null or empty.")
  private final Long createdOn;

  public DocumentFormDTO(String document, String documentFormat, String signature, Long createdOn) {
    this.document = document;
    this.documentFormat = documentFormat;
    this.signature = signature;
    this.createdOn = createdOn;
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
